package com.example.weathery.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.repo.IWeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(
    private val repository: IWeatherRepository
) : ViewModel() {

    private val _language = MutableStateFlow(repository.getLanguage())
    val language: StateFlow<String> = _language

    private val _unitSystem = MutableStateFlow(repository.getUnitSystem())
    val unitSystem: StateFlow<String> = _unitSystem

    private val _locationSource = MutableStateFlow(repository.getLocationSource())
    val locationSource: StateFlow<String> = _locationSource

    val tempUnit: StateFlow<String> = _unitSystem.map {
        when (it) {
            "metric" -> "°C"
            "imperial" -> "°F"
            else -> "K"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "°C")

    val windSpeedUnit: StateFlow<String> = _unitSystem.map {
        when (it) {
            "imperial" -> "mph"
            else -> "m/s"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "m/s")

    fun getEffectiveLanguage(): String {
        return if (_language.value == "device") Locale.getDefault().language else _language.value
    }

    fun setLanguage(value: String) {
        _language.value = value
        repository.setLanguage(value)
    }

    fun setUnitSystem(value: String) {
        _unitSystem.value = value
        repository.setUnitSystem(value)
    }

    fun setLocationSource(value: String) {
        _locationSource.value = value
        repository.setLocationSource(value)
    }

    fun setPickedLocation(lat: Double, lon: Double, city: String) {
        repository.setPickedLocation(lat, lon, city)
    }

    fun getPickedLocation(): Triple<Double, Double, String>? {
        return repository.getPickedLocation()
    }

    class Factory(private val repo: IWeatherRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repo) as T
        }
    }
}
