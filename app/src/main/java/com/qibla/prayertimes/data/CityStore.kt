package com.qibla.prayertimes.data

import android.content.Context
import com.qibla.prayertimes.model.City
import org.json.JSONArray
import org.json.JSONObject

class CityStore(context: Context) {
    private val prefs = context.getSharedPreferences("qibla_app_prefs", Context.MODE_PRIVATE)

    fun loadCustomCities(): List<City> {
        val raw = prefs.getString(KEY_CUSTOM_CITIES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                City(o.getString("name"), o.getDouble("lat"), o.getDouble("lon"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomCities(cities: List<City>) {
        val arr = JSONArray()
        cities.forEach {
            val o = JSONObject()
            o.put("name", it.name)
            o.put("lat", it.lat)
            o.put("lon", it.lon)
            arr.put(o)
        }
        prefs.edit().putString(KEY_CUSTOM_CITIES, arr.toString()).apply()
    }

    fun loadSelectedCity(): City? {
        val raw = prefs.getString(KEY_SELECTED_CITY, null) ?: return null
        return try {
            val o = JSONObject(raw)
            City(o.getString("name"), o.getDouble("lat"), o.getDouble("lon"))
        } catch (e: Exception) {
            null
        }
    }

    fun saveSelectedCity(city: City) {
        val o = JSONObject()
        o.put("name", city.name)
        o.put("lat", city.lat)
        o.put("lon", city.lon)
        prefs.edit().putString(KEY_SELECTED_CITY, o.toString()).apply()
    }

    companion object {
        private const val KEY_CUSTOM_CITIES = "custom_cities"
        private const val KEY_SELECTED_CITY = "selected_city"
    }
}
