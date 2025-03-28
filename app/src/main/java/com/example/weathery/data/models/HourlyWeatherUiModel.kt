package com.example.weathery.data.models

data class HourlyWeatherUiModel(
    val time: String,
    val iconRes: Int,
    val temp: Int,
    val feelsLike: Int,
    val description: String
)



