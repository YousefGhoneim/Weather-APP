package com.example.weathery.data.remote

import com.example.weathery.data.models.Current
import com.example.weathery.data.models.DailyItem
import com.example.weathery.data.models.FeelsLike
import com.example.weathery.data.models.HourlyItem
import com.example.weathery.data.models.OneResponse
import com.example.weathery.data.models.OneResponse.*
import com.example.weathery.data.models.Temp
import com.example.weathery.data.models.WeatherItem
import com.example.weathery.data.remote.IRemoteDataSource

class FakeRemoteDataSource : IRemoteDataSource {

    var shouldFail = false
    var lastRequestedUnit: String? = null

    override suspend fun getWeather(lat: Double, lon: Double, units: String): OneResponse {
        if (shouldFail) throw Exception("Simulated network failure")
        lastRequestedUnit = units

        return OneResponse(
            lat = lat,
            lon = lon,
            timezone = "Fake/Timezone",
            current = Current(
                temp = 28.0,
                feelsLike = 30.0,
                weather = listOf(WeatherItem(icon = "01d", main = "Clear", description = "Sunny")),
                sunrise = 1712280000,
                sunset = 1712323200,
                humidity = 40,
                pressure = 1012,
                windSpeed = 5.5,
                uvi = 7.2,
                clouds = 10
            ),
            hourly = listOf(
                HourlyItem(
                    dt = 1712300000,
                    temp = 28.5,
                    feelsLike = 29.5,
                    weather = listOf(WeatherItem(icon = "01d", description = "Clear", main = "Clear")),
                    windSpeed = 5.2
                )
            ),
            daily = listOf(
                DailyItem(
                    dt = 1712300000,
                    temp = Temp(min = 20.0, max = 34.0),
                    feelsLike = FeelsLike(day = 30.0),
                    weather = listOf(WeatherItem(icon = "01d", main = "Clear", description = "Sunny")),
                    windSpeed = 4.7,
                    pressure = 1010,
                    humidity = 35,
                    uvi = 6.8
                )
            ),
            minutely = null
        )
    }
}

