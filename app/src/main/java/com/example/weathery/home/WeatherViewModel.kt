package com.example.weathery.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.models.UiState
import com.example.weathery.data.models.WeatherUiModel
import com.example.weathery.data.repo.WeatherRepository
import com.example.weathery.utils.toWeatherUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<WeatherUiModel>>(UiState.Loading)
    val uiState: StateFlow<UiState<WeatherUiModel>> = _uiState

    fun fetchWeather(lat: Double, lon: Double, cityName: String, units: String = "metric") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = repository.fetchWeather(lat, lon, units)
                val mapped = response.toWeatherUiModel(cityName, units)
                repository.cacheWeather(cityName, mapped) // Save to local cache
                _uiState.value = UiState.Success(mapped)
            } catch (e: Exception) {
                // On failure, try loading cached weather
                val cached = repository.getCachedWeather()
                if (cached != null) {
                    _uiState.value = UiState.Success(cached)
                } else {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

    class WeatherViewModelFactory(
        private val repository: WeatherRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
