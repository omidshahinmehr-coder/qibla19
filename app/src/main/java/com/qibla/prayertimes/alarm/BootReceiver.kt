package com.qibla.prayertimes.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qibla.prayertimes.work.PrayerTimesWorker
import com.qibla.prayertimes.work.WidgetRefreshWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            PrayerTimesWorker.schedulePeriodic(context)
            PrayerTimesWorker.runOnce(context)
            WidgetRefreshWorker.schedulePeriodic(context)
        }
    }
}
