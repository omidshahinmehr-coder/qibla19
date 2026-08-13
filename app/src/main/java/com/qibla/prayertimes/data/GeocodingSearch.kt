package com.qibla.prayertimes.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class GeocodeResult(val displayName: String, val lat: Double, val lon: Double)

/**
 * Looks up a place name using the free Nominatim (OpenStreetMap) search API. Used only as a
 * fallback when the user's query doesn't match anything in the built-in city list — the app's
 * core features (Qibla, prayer times, adding a city by coordinates or map tap) all keep working
 * with zero internet; this is purely an optional convenience when online.
 */
class GeocodingSearch {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun search(context: Context, query: String): List<GeocodeResult> = withContext(Dispatchers.IO) {
        try {
            val language = context.resources.configuration.locales[0].language
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=0&limit=6&accept-language=$language"
            val request = Request.Builder()
                .url(url)
                // Nominatim's usage policy requires a descriptive User-Agent identifying the app.
                .header("User-Agent", "QiblaPrayerTimesApp/1.0 (contact: Omidshahinmehr@gmail.com)")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val arr = JSONArray(body)
                (0 until arr.length()).mapNotNull { i ->
                    val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                    val lat = obj.optString("lat").toDoubleOrNull() ?: return@mapNotNull null
                    val lon = obj.optString("lon").toDoubleOrNull() ?: return@mapNotNull null
                    val name = obj.optString("display_name", query)
                    GeocodeResult(name, lat, lon)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
