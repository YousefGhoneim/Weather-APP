package com.example.weathery.data.models

data class CurrentWeatherUiModel(
    val temp: Int,
    val feelsLike: Int,
    val description: String,
    val iconRes: Int,
    val date: String,
    val cityName: String,
    val sunrise: String,
    val sunset: String,
    val main: String
)

