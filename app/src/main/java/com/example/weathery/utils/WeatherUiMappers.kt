package com.example.weathery.utils

import android.util.Log
import com.example.weathery.R
import com.example.weathery.data.models.*


fun OneResponse.toWeatherUiModel(cityName: String): WeatherUiModel {
    val current = current ?: throw IllegalStateException("Current weather is missing")
    val dailyList = daily?.filterNotNull() ?: emptyList()
    val hourlyList = hourly?.filterNotNull() ?: emptyList()

    return WeatherUiModel(
        current = CurrentWeatherUiModel(
            temp = current.temp?.toInt() ?: 0,
            feelsLike = current.feelsLike?.toInt() ?: 0,
            description = current.weather?.firstOrNull()?.description ?: "",
            iconRes = getWeatherIconRes(current.weather?.firstOrNull()?.icon),
            date = current.dt?.toFormatted("EEE, dd MMM") ?: "",
            cityName = cityName,
            sunrise = current.sunrise?.toFormatted("hh:mm a") ?: "--",
            sunset = current.sunset?.toFormatted("hh:mm a") ?: "--",
            main = current.weather?.firstOrNull()?.main ?: ""
        ),
        hourly = hourlyList.take(12).map {
            HourlyWeatherUiModel(
                time = it.dt?.toFormatted("hh a") ?: "",
                iconRes = getWeatherIconRes(it.weather?.firstOrNull()?.icon),
                temp = it.temp?.toInt() ?: 0,
                feelsLike = it.feelsLike?.toInt() ?: 0,
                description = it.weather?.firstOrNull()?.description ?: ""
            )
        }
        ,
        daily = dailyList.map {
            DailyWeatherUiModel(
                dayName = it.dt?.toFormatted("EEEE") ?: "",
                date = it.dt?.toFormatted("dd MMM") ?: "",
                maxTemp = it.temp?.max?.toInt() ?: 0,
                minTemp = it.temp?.min?.toInt() ?: 0,
                feelsLike = it.feelsLike?.day?.toInt() ?: 0,
                iconRes = getWeatherIconRes(it.weather?.firstOrNull()?.icon),
                description = it.weather?.firstOrNull()?.description ?: ""
            )
        }

        ,
        extra = ExtraMetricsUiModel(
            pressure = current.pressure ?: 0,
            windSpeed = current.windSpeed ?: 0.0,
            humidity = current.humidity ?: 0,
            uvi = current.uvi ?: 0.0,
            clouds = current.clouds ?: 0,
            windUnit = "m/s" // Default; should come from settings if dynamic
        )
    )
}


fun getWeatherIconRes(iconCode: String?): Int {
    return when (iconCode) {
        "01d" -> R.drawable.ic_clear_day
        "01n" -> R.drawable.ic_clear_night
        "02d", "02n" -> R.drawable.ic_partly_cloudy
        "03d", "03n" -> R.drawable.ic_cloudy
        "04d", "04n" -> R.drawable.ic_cloudy
        "09d", "09n" -> R.drawable.ic_rain
        "10d", "10n" -> R.drawable.ic_rain
        "11d", "11n" -> R.drawable.ic_thunderstorm
        "13d", "13n" -> R.drawable.ic_snow
        "50d", "50n" -> R.drawable.ic_fog
        else -> R.drawable.ic_weather_placeholder

    }
}
