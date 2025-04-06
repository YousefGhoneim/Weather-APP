package com.example.weathery.data.local

import android.content.Context

interface ISettingsPreferences {
    fun getLanguage(): String
    fun setLanguage(language: String)
    fun getUnitSystem(): String
    fun setUnitSystem(system: String)
    fun getLocationSource(): String
    fun setLocationSource(source: String)
    fun getPickedLocation(): Triple<Double, Double, String>?
    fun setPickedLocation(lat: Double, lon: Double, city: String)
}

class SettingsPreferences(context: Context) : ISettingsPreferences {

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    override fun getLanguage(): String = prefs.getString("language", "en") ?: "en"
    override fun setLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
    }

    override fun getUnitSystem(): String = prefs.getString("unit_system", "metric") ?: "metric"
    override fun setUnitSystem(system: String) {
        prefs.edit().putString("unit_system", system).apply()
    }

    override fun getLocationSource(): String = prefs.getString("location_source", "gps") ?: "gps"
    override fun setLocationSource(source: String) {
        prefs.edit().putString("location_source", source).apply()
    }

    override fun getPickedLocation(): Triple<Double, Double, String>? {
        val lat = prefs.getFloat("map_lat", Float.MIN_VALUE)
        val lon = prefs.getFloat("map_lon", Float.MIN_VALUE)
        val city = prefs.getString("map_city", null)
        return if (lat != Float.MIN_VALUE && lon != Float.MIN_VALUE && city != null) {
            Triple(lat.toDouble(), lon.toDouble(), city)
        } else null
    }

    override fun setPickedLocation(lat: Double, lon: Double, city: String) {
        prefs.edit()
            .putFloat("map_lat", lat.toFloat())
            .putFloat("map_lon", lon.toFloat())
            .putString("map_city", city)
            .apply()
    }
}
