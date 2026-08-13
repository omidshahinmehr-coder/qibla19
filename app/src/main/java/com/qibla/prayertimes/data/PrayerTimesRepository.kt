package com.qibla.prayertimes.data

import android.content.Context
import com.qibla.prayertimes.R
import com.qibla.prayertimes.util.HijriCalendar
import com.qibla.prayertimes.util.LocalePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** [monthAr] is used for both Arabic and Persian locales (they share Hijri month names); [monthEn] for English. */
data class HijriDate(
    val day: String,
    val monthAr: String,
    val monthEn: String,
    val year: String,
    val monthNumber: Int
) {
    /** Month name in whichever language is currently active (see [LocalePrefs.currentLanguage]). */
    fun monthName(context: Context): String =
        if (LocalePrefs.currentLanguage(context) == "en") monthEn else monthAr
}

data class PrayerTimesResult(
    val timings: Map<String, String>,
    val hijri: HijriDate?,
    /** True when these times came from the local offline calculator, not the network API. */
    val isOffline: Boolean = false
)

sealed class PrayerTimesState {
    object Loading : PrayerTimesState()
    data class Success(val result: PrayerTimesResult) : PrayerTimesState()
    object Error : PrayerTimesState()
}

/** Order in which prayer times are displayed, matching the labels below. */
val PRAYER_ORDER = listOf("Imsak", "Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha", "Midnight")

private val PRAYER_LABEL_RES = mapOf(
    "Imsak" to R.string.prayer_imsak,
    "Fajr" to R.string.prayer_fajr,
    "Sunrise" to R.string.prayer_sunrise,
    "Dhuhr" to R.string.prayer_dhuhr,
    "Asr" to R.string.prayer_asr,
    "Sunset" to R.string.prayer_sunset,
    "Maghrib" to R.string.prayer_maghrib,
    "Isha" to R.string.prayer_isha,
    "Midnight" to R.string.prayer_midnight
)

/** Localized prayer names, following the app's current language (Persian/English/Arabic). */
fun prayerLabels(context: Context): Map<String, String> =
    PRAYER_LABEL_RES.mapValues { (_, resId) -> context.getString(resId) }

class PrayerTimesRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches today's prayer times from the Aladhan API, using whichever [PrayerCalculationMethod]
     * is passed in. The Hijri date comes from the same online response, then [HijriCalendar.shift]
     * applies [hijriCorrectionDays] to it ourselves — the API's own `adjustment` query parameter
     * turned out to be unreliable in practice, so this app no longer depends on it. If the
     * network is unreachable or the request fails for any reason, silently falls back to
     * [OfflinePrayerCalculator] (and a local Hijri calculation, shifted the same way) so the
     * app still works with no internet connection — the result is flagged with
     * [PrayerTimesResult.isOffline] so the UI can note it's approximate.
     */
    suspend fun fetchToday(
        lat: Double,
        lon: Double,
        method: PrayerCalculationMethod = PrayerCalculationMethod.DEFAULT,
        hijriCorrectionDays: Int = 0
    ): PrayerTimesState = withContext(Dispatchers.IO) {
        try {
            fetchOnline(lat, lon, method, hijriCorrectionDays)
        } catch (e: Exception) {
            try {
                PrayerTimesState.Success(computeOffline(lat, lon, method, hijriCorrectionDays))
            } catch (offlineError: Exception) {
                PrayerTimesState.Error
            }
        }
    }

    private fun fetchOnline(lat: Double, lon: Double, method: PrayerCalculationMethod, hijriCorrectionDays: Int): PrayerTimesState {
        val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        val url = "https://api.aladhan.com/v1/timings/$dateStr?latitude=$lat&longitude=$lon" +
            "&method=${method.aladhanMethodId}&midnightMode=1"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return PrayerTimesState.Success(computeOffline(lat, lon, method, hijriCorrectionDays))
            }
            val body = response.body?.string()
                ?: return PrayerTimesState.Success(computeOffline(lat, lon, method, hijriCorrectionDays))
            val json = JSONObject(body)
            val data = json.getJSONObject("data")
            val timingsJson = data.getJSONObject("timings")
            val timings = mutableMapOf<String, String>()
            for (key in PRAYER_ORDER) {
                val raw = timingsJson.optString(key, "--:--")
                timings[key] = raw.split(" ").firstOrNull() ?: raw
            }
            val hijriJson = data.optJSONObject("date")?.optJSONObject("hijri")
            val hijri = hijriJson?.let {
                val rawDay = it.optString("day", "").toIntOrNull() ?: 0
                val rawMonth = it.optJSONObject("month")?.optInt("number", 0) ?: 0
                val rawYear = it.optString("year", "").toIntOrNull() ?: 0
                if (rawDay > 0 && rawMonth in 1..12 && rawYear > 0) {
                    // Shift ourselves, then always take the month *name* from our own lookup
                    // (not the API's original strings) — if the correction rolls the date over
                    // into the next/previous month, the API's original name strings would be
                    // stale for the shifted month.
                    val shifted = HijriCalendar.shift(rawYear, rawMonth, rawDay, hijriCorrectionDays)
                    HijriDate(
                        day = shifted.day.toString(),
                        monthAr = shifted.monthNameAr,
                        monthEn = shifted.monthNameEn,
                        year = shifted.year.toString(),
                        monthNumber = shifted.month
                    )
                } else {
                    // Malformed/missing fields from the API — show it unshifted rather than fail.
                    HijriDate(
                        day = it.optString("day", ""),
                        monthAr = it.optJSONObject("month")?.optString("ar", "") ?: "",
                        monthEn = it.optJSONObject("month")?.optString("en", "") ?: "",
                        year = it.optString("year", ""),
                        monthNumber = rawMonth
                    )
                }
            }
            return PrayerTimesState.Success(PrayerTimesResult(timings, hijri, isOffline = false))
        }
    }

    private fun computeOffline(lat: Double, lon: Double, method: PrayerCalculationMethod, hijriCorrectionDays: Int): PrayerTimesResult {
        val timings = OfflinePrayerCalculator.computeToday(lat, lon, method)
        val h = HijriCalendar.today(hijriCorrectionDays)
        val hijri = HijriDate(day = h.day.toString(), monthAr = h.monthNameAr, monthEn = h.monthNameEn, year = h.year.toString(), monthNumber = h.month)
        return PrayerTimesResult(timings, hijri, isOffline = true)
    }
}
