package com.example.weathery.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.data.repo.IWeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: IWeatherRepository) : ViewModel() {

    private val _favoritesState = MutableStateFlow<UiState<List<CityEntity>>>(UiState.Loading)
    val favoritesState: StateFlow<UiState<List<CityEntity>>> = _favoritesState

    init {
        getFavorites()
    }

    private fun getFavorites() {
        viewModelScope.launch {
            repository.getFavoriteCities().collect { list ->
                _favoritesState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            }
        }
    }

    fun saveCity(city: CityEntity) {
        viewModelScope.launch {
            repository.saveCity(city)
            getFavorites()
        }
    }

    fun deleteCity(city: CityEntity) {
        viewModelScope.launch {
            repository.deleteCity(city)
            getFavorites()
        }
    }

    class FavoritesViewModelFactory (
        private val repository: IWeatherRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                return FavoritesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
