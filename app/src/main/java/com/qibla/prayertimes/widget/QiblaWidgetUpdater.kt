package com.qibla.prayertimes.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object QiblaWidgetUpdater {

    /** Fire-and-forget: asks every placed instance of both widgets to redraw from cached data. */
    fun requestUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ویجت اصلی
                QiblaWidget().updateAll(context)

                // ⭐ ویجت سبک — این خط مشکل را کامل حل می‌کند
                LightQiblaWidget().updateAll(context)

            } catch (e: Exception) {
                // No widget currently placed on any home screen, or a transient Glance error — safe to ignore.
            }
        }
    }
}
