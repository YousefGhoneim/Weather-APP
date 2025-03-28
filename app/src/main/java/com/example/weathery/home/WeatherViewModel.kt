package com.example.weathery.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.models.OneResponse
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

    fun fetchWeather(lat: Double, lon: Double, cityName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val response = repository.fetchWeather(lat, lon)
                val mapped = response.toWeatherUiModel(cityName)
                _uiState.value = UiState.Success(mapped)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    class WeatherViewModelFactory(
        private val repository: WeatherRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel( repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

