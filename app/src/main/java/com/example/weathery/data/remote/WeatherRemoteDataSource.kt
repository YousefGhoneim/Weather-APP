package com.example.weathery.data.remote

import com.example.weathery.data.models.OneResponse

class WeatherRemoteDataSource(private val apiService: WeatherApiService) {
    suspend fun getWeather(lat: Double, lon: Double): OneResponse {
        return apiService.getWeather(lat, lon)
    }
}
