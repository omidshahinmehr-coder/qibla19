package com.qibla.prayertimes.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopAdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AdhanPlaybackService.stopNow(context)
    }
}
