package com.qibla.prayertimes.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import com.qibla.prayertimes.alarm.AlarmScheduler
import com.qibla.prayertimes.data.CityStore
import com.qibla.prayertimes.data.PrayerMethodPrefs
import com.qibla.prayertimes.data.PrayerTimesRepository
import com.qibla.prayertimes.data.PrayerTimesState
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.util.HijriCorrectionPrefs
import com.qibla.prayertimes.widget.QiblaWidgetUpdater
import com.qibla.prayertimes.widget.LightQiblaWidgetUpdater
import java.util.concurrent.TimeUnit

/**
 * Refreshes today's prayer times in the background, then:
 *  1. caches them for the widget and for the alarm settings screen,
 *  2. re-schedules today's adhan alarms, and
 *  3. asks the Glance widget to redraw.
 *
 * Runs once on boot (via BootReceiver) and once daily thereafter, so alarms and the
 * widget stay correct even if the user never opens the app that day.
 */
class PrayerTimesWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val city = CityStore(applicationContext).loadSelectedCity() ?: defaultCities(applicationContext).first()
        val method = PrayerMethodPrefs.get(applicationContext)
        val correctionDays = HijriCorrectionPrefs.get(applicationContext)
        val state = PrayerTimesRepository().fetchToday(city.lat, city.lon, method, correctionDays)

        return when (state) {
            is PrayerTimesState.Success -> {
                WidgetDataStore(applicationContext).save(city.name, state.result.timings, state.result.hijri, state.result.isOffline)
                AlarmScheduler.scheduleToday(applicationContext, state.result.timings)
                QiblaWidgetUpdater.requestUpdate(applicationContext)
                // ⭐ ویجت سبک ← این خط مشکل را ۱۰۰٪ حل می‌کند
    LightQiblaWidgetUpdater.requestUpdate(applicationContext)
    // یا:
    // LightQiblaWidget().updateAll(applicationContext)

                Result.success()
            }
            is PrayerTimesState.Error -> Result.retry()
            is PrayerTimesState.Loading -> Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_PERIODIC_NAME = "qibla_daily_refresh"

        fun runOnce(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<PrayerTimesWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }

        /** Schedules (or re-confirms) the recurring daily refresh. Safe to call repeatedly. */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<PrayerTimesWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
