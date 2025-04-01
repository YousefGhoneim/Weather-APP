package com.example.weathery.data.remote

import com.example.weathery.data.models.OneResponse

class WeatherRemoteDataSource(private val apiService: WeatherApiService) {
    suspend fun getWeather(lat: Double, lon: Double ,units: String = "metric"): OneResponse {
        return apiService.getWeather(lat, lon , units=units)
    }
}
