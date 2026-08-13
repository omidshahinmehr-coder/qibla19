package com.qibla.prayertimes.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.qibla.prayertimes.R
import com.qibla.prayertimes.model.City
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCity(): City? = suspendCancellableCoroutine { cont ->
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = try { locationManager.getLastKnownLocation(provider) } catch (e: SecurityException) { null }
            if (loc != null && (bestLocation == null || loc.accuracy < bestLocation!!.accuracy)) {
                bestLocation = loc
            }
        }

        if (bestLocation != null) {
            cont.resume(toCity(bestLocation))
            return@suspendCancellableCoroutine
        }

        // No cached fix available: request a single fresh update from the best provider.
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val listener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (cont.isActive) cont.resume(toCity(location))
            }
        }
        try {
            locationManager.requestSingleUpdate(provider, listener, null)
        } catch (e: SecurityException) {
            cont.resume(null)
        }
        cont.invokeOnCancellation { locationManager.removeUpdates(listener) }
    }

    private fun toCity(location: Location): City {
        val name = reverseGeocode(location.latitude, location.longitude)
            ?: context.getString(R.string.current_location_name)
        return City(name, location.latitude, location.longitude)
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(context, context.resources.configuration.locales[0])
            val results = geocoder.getFromLocation(lat, lon, 1)
            val address = results?.firstOrNull()
            address?.locality ?: address?.subAdminArea ?: address?.adminArea
        } catch (e: Exception) {
            null
        }
    }
}
