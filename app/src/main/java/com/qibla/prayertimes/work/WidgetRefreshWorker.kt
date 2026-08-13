package com.qibla.prayertimes.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.qibla.prayertimes.widget.QiblaWidgetUpdater
import java.util.concurrent.TimeUnit

/**
 * Redraws the home screen widget from already-cached data every 15 minutes (the minimum
 * interval WorkManager allows for periodic work) so the "next prayer" countdown stays
 * reasonably current without any network call or noticeable battery cost.
 */
class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        QiblaWidgetUpdater.requestUpdate(applicationContext)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "qibla_widget_refresh"

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
