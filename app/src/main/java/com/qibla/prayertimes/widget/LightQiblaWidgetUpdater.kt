package com.qibla.prayertimes.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LightQiblaWidgetUpdater {

    fun requestUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                LightQiblaWidget().updateAll(context)
            } catch (_: Exception) { }
        }
    }
}
