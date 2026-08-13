package com.qibla.prayertimes.alarm

import android.content.Context
import android.net.Uri
import com.qibla.prayertimes.R

/** Prayers that can have an adhan alarm. Sunrise/Sunset/Imsak/Midnight are informational only. */
enum class AdhanPrayer(val timingsKey: String, val labelRes: Int, val requestCode: Int) {
    FAJR("Fajr", R.string.prayer_fajr, 101),
    DHUHR("Dhuhr", R.string.prayer_dhuhr, 102),
    ASR("Asr", R.string.prayer_asr, 103),
    MAGHRIB("Maghrib", R.string.prayer_maghrib, 104),
    ISHA("Isha", R.string.prayer_isha, 105);

    /** Request code for this prayer's separate "reminder before adhan" alarm. */
    val reminderRequestCode: Int get() = requestCode + 500

    /** Localized display name, following the app's current language. */
    fun label(context: Context): String = context.getString(labelRes)
}

/** Reminder lead times offered in the UI; 0 means "off". */
val REMINDER_OPTIONS = listOf(0, 5, 10, 15, 20, 30)

data class AlarmSettings(val enabled: Boolean, val soundUri: Uri?)

class AlarmPrefs(private val context: Context) {
    private val prefs = context.getSharedPreferences("qibla_alarm_prefs", Context.MODE_PRIVATE)

    fun isEnabled(prayer: AdhanPrayer): Boolean =
        prefs.getBoolean(enabledKey(prayer), false)

    fun setEnabled(prayer: AdhanPrayer, enabled: Boolean) {
        prefs.edit().putBoolean(enabledKey(prayer), enabled).apply()
    }

    fun soundUri(prayer: AdhanPrayer): Uri {
        val raw = prefs.getString(soundKey(prayer), null)
        return if (raw != null) Uri.parse(raw) else defaultSoundUri()
    }

    fun setSoundUri(prayer: AdhanPrayer, uri: Uri) {
        prefs.edit().putString(soundKey(prayer), uri.toString()).apply()
    }

    /** Minutes before the adhan to show a reminder notification; 0 means no reminder. */
    fun reminderMinutes(prayer: AdhanPrayer): Int =
        prefs.getInt(reminderKey(prayer), 0)

    fun setReminderMinutes(prayer: AdhanPrayer, minutes: Int) {
        prefs.edit().putInt(reminderKey(prayer), minutes).apply()
    }

    fun settingsFor(prayer: AdhanPrayer): AlarmSettings =
        AlarmSettings(isEnabled(prayer), soundUri(prayer))

    /**
     * The sound used for any prayer the user hasn't explicitly picked a sound for. Points at
     * `res/raw/adhan_default.mp3` — replace that file with the real adhan recording and it
     * becomes the default automatically. Falls back to the system alarm ringtone only if that
     * resource is ever removed.
     */
    private fun defaultSoundUri(): Uri =
        Uri.parse("android.resource://${context.packageName}/${R.raw.adhan_default}")

    private fun enabledKey(prayer: AdhanPrayer) = "enabled_${prayer.name}"
    private fun soundKey(prayer: AdhanPrayer) = "sound_${prayer.name}"
    private fun reminderKey(prayer: AdhanPrayer) = "reminder_${prayer.name}"
}
