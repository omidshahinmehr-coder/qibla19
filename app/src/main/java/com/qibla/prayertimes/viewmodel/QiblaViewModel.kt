package com.qibla.prayertimes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qibla.prayertimes.alarm.AlarmScheduler
import com.qibla.prayertimes.data.CityStore
import com.qibla.prayertimes.data.LocationHelper
import com.qibla.prayertimes.data.PrayerCalculationMethod
import com.qibla.prayertimes.data.PrayerMethodPrefs
import com.qibla.prayertimes.data.PrayerTimesRepository
import com.qibla.prayertimes.data.PrayerTimesState
import com.qibla.prayertimes.data.QiblaMath
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.model.City
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.model.localizedCatalogName
import com.qibla.prayertimes.util.HijriCorrectionPrefs
import com.qibla.prayertimes.widget.QiblaWidgetUpdater
import com.qibla.prayertimes.work.PrayerTimesWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QiblaViewModel(application: Application) : AndroidViewModel(application) {

    private val cityStore = CityStore(application)
    private val locationHelper = LocationHelper(application)
    private val prayerRepo = PrayerTimesRepository()
    private val widgetDataStore = WidgetDataStore(application)

    private val _selectedCity = MutableStateFlow(cityStore.loadSelectedCity() ?: defaultCities(application).first())
    val selectedCity: StateFlow<City> = _selectedCity.asStateFlow()

    private val _customCities = MutableStateFlow(cityStore.loadCustomCities())
    val customCities: StateFlow<List<City>> = _customCities.asStateFlow()

    private val _prayerState = MutableStateFlow<PrayerTimesState>(PrayerTimesState.Loading)
    val prayerState: StateFlow<PrayerTimesState> = _prayerState.asStateFlow()

    private val _locating = MutableStateFlow(false)
    val locating: StateFlow<Boolean> = _locating.asStateFlow()

    private val _hijriCorrectionDays = MutableStateFlow(HijriCorrectionPrefs.get(application))
    val hijriCorrectionDays: StateFlow<Int> = _hijriCorrectionDays.asStateFlow()

    private val _prayerMethod = MutableStateFlow(PrayerMethodPrefs.get(application))
    val prayerMethod: StateFlow<PrayerCalculationMethod> = _prayerMethod.asStateFlow()

    val bearing: Double get() = QiblaMath.bearing(_selectedCity.value.lat, _selectedCity.value.lon)
    val distanceKm: Int get() = QiblaMath.distanceKm(_selectedCity.value.lat, _selectedCity.value.lon)

    init {
        refreshPrayerTimes()
        PrayerTimesWorker.schedulePeriodic(application)
        com.qibla.prayertimes.work.WidgetRefreshWorker.schedulePeriodic(application)
    }

    /**
     * Re-derives the selected city's display name for the current in-app language. The name
     * saved to [cityStore] is a plain string frozen in whatever language was active at
     * selection time, so a language change doesn't update it on its own — this must be called
     * (e.g. once per composition of the home screen) to pick up the new locale after
     * [com.qibla.prayertimes.util.LocalePrefs] changes and the activity recreates. Custom /
     * geocoded cities aren't in the built-in catalog, so their names are left untouched.
     *
     * Also re-localizes the name cached for the home screen widgets, so they don't keep
     * showing the old-language name until their next full data refresh.
     */
    fun syncCityNameWithLocale() {
        val current = _selectedCity.value
        val localizedName = localizedCatalogName(getApplication(), current.lat, current.lon)
        if (localizedName != null && localizedName != current.name) {
            val updated = current.copy(name = localizedName)
            _selectedCity.value = updated
            cityStore.saveSelectedCity(updated)

            val app = getApplication<Application>()
            widgetDataStore.updateCityName(localizedName)
            QiblaWidgetUpdater.requestUpdate(app)
        }
    }

    /**
     * Applies a new manual Hijri correction (see [HijriCorrectionPrefs]): persists it, then
     * re-fetches today's data so the correction is applied consistently — [HijriCalendar.shift]
     * applies it to whichever date (online or offline) the repository would otherwise show —
     * updating the widgets as part of the same refresh.
     */
    fun setHijriCorrection(days: Int) {
        val clamped = days.coerceIn(HijriCorrectionPrefs.MIN_OFFSET, HijriCorrectionPrefs.MAX_OFFSET)
        HijriCorrectionPrefs.set(getApplication(), clamped)
        _hijriCorrectionDays.value = clamped
        refreshPrayerTimes()
    }

    /**
     * Switches the calculation method (see [PrayerCalculationMethod]): persists it, then
     * immediately re-fetches today's prayer times with the new method — which also refreshes
     * the widgets and re-schedules today's adhan alarms against the new times.
     */
    fun setPrayerMethod(method: PrayerCalculationMethod) {
        val app = getApplication<Application>()
        PrayerMethodPrefs.set(app, method)
        _prayerMethod.value = method
        refreshPrayerTimes()
    }

    fun selectCity(city: City) {
        _selectedCity.value = city
        cityStore.saveSelectedCity(city)
        refreshPrayerTimes()
    }

    fun refreshPrayerTimes() {
        val city = _selectedCity.value
        _prayerState.value = PrayerTimesState.Loading
        viewModelScope.launch {
            val result = prayerRepo.fetchToday(city.lat, city.lon, _prayerMethod.value, _hijriCorrectionDays.value)
            _prayerState.value = result
            if (result is PrayerTimesState.Success) {
                val app = getApplication<Application>()
                widgetDataStore.save(city.name, result.result.timings, result.result.hijri, result.result.isOffline)
                AlarmScheduler.scheduleToday(app, result.result.timings)
                QiblaWidgetUpdater.requestUpdate(app)
            }
        }
    }

    fun addCustomCity(city: City) {
        val updated = _customCities.value + city
        _customCities.value = updated
        cityStore.saveCustomCities(updated)
        selectCity(city)
    }

    fun removeCustomCity(city: City) {
        val updated = _customCities.value.filterNot { it.name == city.name && it.lat == city.lat && it.lon == city.lon }
        _customCities.value = updated
        cityStore.saveCustomCities(updated)
    }

    fun locateMe() {
        viewModelScope.launch {
            _locating.value = true
            val city = locationHelper.getCurrentCity()
            if (city != null) selectCity(city)
            _locating.value = false
        }
    }
}
