package com.example.weathery.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.models.UiState
import com.example.weathery.data.models.WeatherUiModel
import com.example.weathery.data.repo.IWeatherRepository
import com.example.weathery.utils.toWeatherUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: IWeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<WeatherUiModel>>(UiState.Loading)
    val uiState: StateFlow<UiState<WeatherUiModel>> = _uiState

    fun fetchWeather(lat: Double, lon: Double, cityName: String) {
        val unitSystem = repository.getUnitSystem()
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val response = repository.fetchWeather(lat, lon, unitSystem)
                val mapped = response.toWeatherUiModel(cityName, unitSystem)
                repository.cacheWeather(cityName, mapped)
                _uiState.value = UiState.Success(mapped)
            } catch (e: Exception) {
                val cached = repository.getCachedWeather()
                if (cached != null) {
                    _uiState.value = UiState.Success(cached)
                } else {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

    class Factory(private val repo: IWeatherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repo) as T
        }
    }
}
