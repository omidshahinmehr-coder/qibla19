package com.qibla.prayertimes.data

import android.content.Context

/**
 * The convention used to compute prayer times — which angles below the horizon define Fajr,
 * Maghrib and Isha. Threaded through both the online calculator (as the Aladhan API's
 * `method` parameter) and the offline one (as the angles themselves), so switching methods in
 * the menu changes both consistently, online and offline alike.
 *
 * [TEHRAN] (Institute of Geophysics, University of Tehran) has been this app's only method so
 * far. [JAFARI] (Shia Ithna-Ashari / Ja'fari fiqh, per the Leva Research Institute, Qom) uses a
 * slightly narrower Fajr angle and a slightly earlier Maghrib. Isha's angle and the
 * sunset-to-Fajr "Jafari midnight" convention are already shared by both, unchanged by this
 * setting.
 */
enum class PrayerCalculationMethod(
    val key: String,
    val aladhanMethodId: Int,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val maghribAngle: Double
) {
    TEHRAN(key = "tehran", aladhanMethodId = 7, fajrAngle = 17.7, ishaAngle = 14.0, maghribAngle = 4.5),
    JAFARI(key = "jafari", aladhanMethodId = 0, fajrAngle = 16.0, ishaAngle = 14.0, maghribAngle = 4.0);

    companion object {
        val DEFAULT = TEHRAN

        fun fromKey(key: String?): PrayerCalculationMethod =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

object PrayerMethodPrefs {
    private const val PREFS_NAME = "qibla_prayer_method_prefs"
    private const val KEY_METHOD = "prayer_method"

    fun get(context: Context): PrayerCalculationMethod {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_METHOD, null)
        return PrayerCalculationMethod.fromKey(stored)
    }

    fun set(context: Context, method: PrayerCalculationMethod) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_METHOD, method.key).apply()
    }
}
