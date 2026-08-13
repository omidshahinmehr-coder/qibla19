package com.qibla.prayertimes.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DayPrayerTimes(val day: Int, val timings: Map<String, String>)

data class MonthPrayerTimes(
    val year: Int,
    val month: Int,
    val days: List<DayPrayerTimes>,
    val isOffline: Boolean
)

class MonthlyPrayerTimesRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMonth(
        lat: Double,
        lon: Double,
        year: Int,
        month: Int,
        method: PrayerCalculationMethod = PrayerCalculationMethod.DEFAULT
    ): MonthPrayerTimes =
        withContext(Dispatchers.IO) {
            try {
                fetchOnline(lat, lon, year, month, method)
            } catch (e: Exception) {
                computeOffline(lat, lon, year, month, method)
            }
        }

    private fun fetchOnline(lat: Double, lon: Double, year: Int, month: Int, method: PrayerCalculationMethod): MonthPrayerTimes {
        val url = "https://api.aladhan.com/v1/calendar/$year/$month?latitude=$lat&longitude=$lon&method=${method.aladhanMethodId}&midnightMode=1"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return computeOffline(lat, lon, year, month, method)
            val body = response.body?.string() ?: return computeOffline(lat, lon, year, month, method)
            val json = JSONObject(body)
            val dataArray = json.getJSONArray("data")
            val days = (0 until dataArray.length()).map { i ->
                val entry = dataArray.getJSONObject(i)
                val dayNum = entry.getJSONObject("date").getJSONObject("gregorian").getString("day").toInt()
                val timingsJson = entry.getJSONObject("timings")
                val timings = mutableMapOf<String, String>()
                for (key in PRAYER_ORDER) {
                    val raw = timingsJson.optString(key, "--:--")
                    timings[key] = raw.split(" ").firstOrNull() ?: raw
                }
                DayPrayerTimes(dayNum, timings)
            }
            return MonthPrayerTimes(year, month, days, isOffline = false)
        }
    }

    private fun computeOffline(lat: Double, lon: Double, year: Int, month: Int, method: PrayerCalculationMethod): MonthPrayerTimes {
        val daysInMonth = daysInMonth(year, month)
        val days = (1..daysInMonth).map { d ->
            DayPrayerTimes(d, OfflinePrayerCalculator.computeFor(lat, lon, year, month, d, method))
        }
        return MonthPrayerTimes(year, month, days, isOffline = true)
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
