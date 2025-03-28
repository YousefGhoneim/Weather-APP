package com.example.weathery.data.models

data class ExtraMetricsUiModel(
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val uvi: Double,
    val clouds: Int,
    val windUnit: String
)

