package com.example.weathery.data.remote

import com.example.weathery.data.models.OneResponse

class WeatherRemoteDataSource(
    private val apiService: WeatherApiService
) : IRemoteDataSource {

    override suspend fun getWeather(lat: Double, lon: Double, units: String): OneResponse {
        return apiService.getWeather(lat, lon, units = units)
    }
}
