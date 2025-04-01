package com.example.weathery.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_UNIT_SYSTEM = "unit_system"
        private const val KEY_LOCATION_SRC = "location_source"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language

    private val _unitSystem = MutableStateFlow(prefs.getString(KEY_UNIT_SYSTEM, "metric") ?: "metric")
    val unitSystem: StateFlow<String> = _unitSystem

    private val _locationSource = MutableStateFlow(prefs.getString(KEY_LOCATION_SRC, "gps") ?: "gps")
    val locationSource: StateFlow<String> = _locationSource




    fun getEffectiveLanguage(): String {
        return when (_language.value) {
            "device" -> Locale.getDefault().language
            else -> _language.value
        }
    }
    // Derived temperature unit (for display only)
    val tempUnit: StateFlow<String> = _unitSystem.map { unit ->
        when (unit) {
            "metric" -> "°C"
            "imperial" -> "°F"
            else -> "K"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "°C")

    // Derived wind speed unit (for display only)
    val windSpeedUnit: StateFlow<String> = _unitSystem.map { unit ->
        when (unit) {
            "imperial" -> "mph"
            else -> "m/s"  // metric & standard both use m/s
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "m/s")

    fun setLanguage(value: String) {
        _language.value = value
        prefs.edit().putString(KEY_LANGUAGE, value).apply()
    }

    fun setUnitSystem(value: String) {
        _unitSystem.value = value
        prefs.edit().putString(KEY_UNIT_SYSTEM, value).apply()
    }

    fun setLocationSource(value: String) {
        _locationSource.value = value
        prefs.edit().putString(KEY_LOCATION_SRC, value).apply()
    }

    fun setPickedLocation(lat: Double, lon: Double, city: String) {
        prefs.edit()
            .putFloat("map_lat", lat.toFloat())
            .putFloat("map_lon", lon.toFloat())
            .putString("map_city", city)
            .apply()
    }

    fun getPickedLocation(): Triple<Double, Double, String>? {
        val lat = prefs.getFloat("map_lat", Float.MIN_VALUE)
        val lon = prefs.getFloat("map_lon", Float.MIN_VALUE)
        val city = prefs.getString("map_city", null)

        return if (lat != Float.MIN_VALUE && lon != Float.MIN_VALUE && city != null) {
            Triple(lat.toDouble(), lon.toDouble(), city)
        } else null
    }

}
