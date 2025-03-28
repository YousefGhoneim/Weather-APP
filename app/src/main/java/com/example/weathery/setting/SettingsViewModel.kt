package com.example.weathery.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_TEMP_UNIT = "temp_unit"
        private const val KEY_WIND_UNIT = "wind_unit"
        private const val KEY_LOCATION_SRC = "location_source"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language

    private val _tempUnit = MutableStateFlow(prefs.getString(KEY_TEMP_UNIT, "metric") ?: "metric")
    val tempUnit: StateFlow<String> = _tempUnit

    private val _windSpeedUnit = MutableStateFlow(prefs.getString(KEY_WIND_UNIT, "m/s") ?: "m/s")
    val windSpeedUnit: StateFlow<String> = _windSpeedUnit

    private val _locationSource = MutableStateFlow(prefs.getString(KEY_LOCATION_SRC, "gps") ?: "gps")
    val locationSource: StateFlow<String> = _locationSource

    fun setLanguage(value: String) {
        _language.value = value
        prefs.edit().putString(KEY_LANGUAGE, value).apply()
    }

    fun setTempUnit(value: String) {
        _tempUnit.value = value
        prefs.edit().putString(KEY_TEMP_UNIT, value).apply()
    }

    fun setWindSpeedUnit(value: String) {
        _windSpeedUnit.value = value
        prefs.edit().putString(KEY_WIND_UNIT, value).apply()
    }

    fun setLocationSource(value: String) {
        _locationSource.value = value
        prefs.edit().putString(KEY_LOCATION_SRC, value).apply()
    }
}
