package com.example.weathery.data.models

data class DailyWeatherUiModel(
    val dayName: String,
    val date: String,
    val maxTemp: Int,
    val minTemp: Int,
    val feelsLike: Int,
    val iconRes: Int,
    val description: String,
    val windSpeed: Double = 0.0
)


