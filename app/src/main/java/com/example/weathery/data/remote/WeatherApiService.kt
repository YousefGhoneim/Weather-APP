package com.example.weathery.data.remote

import com.example.weathery.data.models.OneResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("onecall")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = "30a73a92f374a05cbcd5f6b8caeacab0",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): OneResponse
}
