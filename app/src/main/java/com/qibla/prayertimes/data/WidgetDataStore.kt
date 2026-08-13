package com.qibla.prayertimes.data

import android.content.Context
import com.qibla.prayertimes.R
import com.qibla.prayertimes.util.JalaliCalendar
import com.qibla.prayertimes.util.LocalePrefs
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WidgetSnapshot(
    val cityName: String,
    val timings: Map<String, String>,
    val hijriText: String,
    val jalaliText: String,
    val gregorianDateKey: String,
    val isOffline: Boolean = false
)

/**
 * Persists the most recently fetched prayer times so they can be read back by:
 *  - the home screen widget (which may render without the app process running), and
 *  - the alarm settings screen, so toggling a prayer on/off can reschedule immediately
 *    using the same data, without a fresh network call.
 */
class WidgetDataStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("qibla_widget_prefs", Context.MODE_PRIVATE)

    fun save(cityName: String, timings: Map<String, String>, hijri: HijriDate?, isOffline: Boolean = false) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        // Always compute the Jalali date and let the display layer (which correctly reads the
        // app's chosen language, including the in-app override) decide whether to show it —
        // checking the language here against the raw system locale caused it to go blank
        // whenever the user had picked an in-app language different from the system one.
        val jalali = JalaliCalendar.today().toString()
        val langContext = LocalePrefs.wrap(appContext)
        val eraSuffix = langContext.getString(R.string.hijri_era_suffix)
        // hijri comes from PrayerTimesRepository: the online Aladhan result (already shifted by
        // the user's correction — see HijriCalendar.shift) when available, otherwise the local
        // offline calculation shifted the same way — so the widget always agrees with the
        // app's home screen and the Hijri-correction screen, regardless of which source was used.
        val hijriText = hijri?.let { "${it.day} ${it.monthName(langContext)} ${it.year}$eraSuffix" } ?: ""

        val timingsJson = JSONObject()
        timings.forEach { (k, v) -> timingsJson.put(k, v) }

        prefs.edit()
            .putString(KEY_CITY, cityName)
            .putString(KEY_TIMINGS, timingsJson.toString())
            .putString(KEY_HIJRI_TEXT, hijriText)
            .putString(KEY_JALALI_TEXT, jalali)
            .putString(KEY_DATE_KEY, todayKey)
            .putBoolean(KEY_OFFLINE, isOffline)
            .apply()
    }

    /**
     * Rewrites just the cached city name, leaving the rest of the snapshot (timings, hijri/
     * jalali text, date key) untouched. Used to re-localize the widget's displayed city name
     * right after an in-app language change, without waiting for the next full prayer-times
     * refresh (which may not happen for hours, or at all without network).
     */
    fun updateCityName(cityName: String) {
        prefs.edit().putString(KEY_CITY, cityName).apply()
    }

    fun load(): WidgetSnapshot? {
        val city = prefs.getString(KEY_CITY, null) ?: return null
        val timingsRaw = prefs.getString(KEY_TIMINGS, null) ?: return null
        val dateKey = prefs.getString(KEY_DATE_KEY, "") ?: ""
        val hijriText = prefs.getString(KEY_HIJRI_TEXT, "") ?: ""
        val jalaliText = prefs.getString(KEY_JALALI_TEXT, "") ?: ""
        val isOffline = prefs.getBoolean(KEY_OFFLINE, false)

        val timings = try {
            val json = JSONObject(timingsRaw)
            val map = mutableMapOf<String, String>()
            json.keys().forEach { key -> map[key] = json.getString(key) }
            map
        } catch (e: Exception) {
            return null
        }

        return WidgetSnapshot(city, timings, hijriText, jalaliText, dateKey, isOffline)
    }

    /** True when the cached snapshot was saved today (Gregorian), i.e. still valid for alarms/widget. */
    fun isFreshToday(): Boolean {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return prefs.getString(KEY_DATE_KEY, null) == todayKey
    }

    companion object {
        private const val KEY_CITY = "city_name"
        private const val KEY_TIMINGS = "timings_json"
        private const val KEY_HIJRI_TEXT = "hijri_text"
        private const val KEY_JALALI_TEXT = "jalali_text"
        private const val KEY_DATE_KEY = "date_key"
        private const val KEY_OFFLINE = "is_offline"
    }
}
