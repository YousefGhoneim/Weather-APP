package com.example.weathery.data.remote

import com.example.weathery.data.models.OneResponse

interface IRemoteDataSource {
    suspend fun getWeather(lat: Double, lon: Double, units: String = "metric"): OneResponse
}
