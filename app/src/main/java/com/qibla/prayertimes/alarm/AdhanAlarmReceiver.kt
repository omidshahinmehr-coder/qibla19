package com.qibla.prayertimes.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.qibla.prayertimes.widget.QiblaWidgetUpdater

/**
 * Receives the AlarmManager broadcast for a prayer time or a pre-adhan reminder.
 *
 * Full adhan alarms hand off to [AdhanPlaybackService] (a foreground service), since a
 * BroadcastReceiver only has a few seconds to finish and playing audio takes longer.
 * Reminders are lightweight — just a plain notification — so they're shown directly here.
 */
class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER) ?: return
        val prayer = AdhanPrayer.entries.firstOrNull { it.name == prayerName } ?: return
        val isReminder = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_REMINDER, false)

        QiblaWidgetUpdater.requestUpdate(context)

        if (isReminder) {
            val minutes = AlarmPrefs(context).reminderMinutes(prayer)
            ReminderNotifier.show(context, prayer, minutes)
            return
        }

        val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_PRAYER, prayerName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
