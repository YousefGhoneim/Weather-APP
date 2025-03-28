package com.example.weathery.data.models

data class WeatherUiModel(
    val current: CurrentWeatherUiModel,
    val hourly: List<HourlyWeatherUiModel>,
    val daily: List<DailyWeatherUiModel>,
    val extra: ExtraMetricsUiModel
)