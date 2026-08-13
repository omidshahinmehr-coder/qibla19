package com.qibla.prayertimes.util

import android.content.Context

/**
 * User-configurable correction applied to the Hijri (lunar) date shown throughout the app and
 * its widgets, to compensate for the gap between arithmetic Hijri calculation and the
 * moon-sighting-based date officially announced where the user lives — these routinely differ
 * by a day, sometimes two, depending on the country and the authority followed. Stored as a
 * signed number of days; 0 (the default) means "no correction".
 */
object HijriCorrectionPrefs {
    private const val PREFS_NAME = "qibla_hijri_prefs"
    private const val KEY_OFFSET = "hijri_offset_days"

    /** The correction is a small nudge, not a general-purpose calendar tool — keep it bounded. */
    const val MIN_OFFSET = -3
    const val MAX_OFFSET = 3

    fun get(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_OFFSET, 0)

    fun set(context: Context, days: Int) {
        val clamped = days.coerceIn(MIN_OFFSET, MAX_OFFSET)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_OFFSET, clamped).apply()
    }
}
