package com.example.weathery.data.models

import com.google.gson.annotations.SerializedName

data class OneResponse(
    val alerts: List<AlertsItem?>? = null,
    val current: Current? = null,
    val timezone: String? = null,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int? = null,
    val daily: List<DailyItem?>? = null,
    val lon: Double? = null,
    val hourly: List<HourlyItem?>? = null,
    val minutely: List<MinutelyItem?>? = null,
    val lat: Double? = null
)

data class AlertsItem(
    val start: Int? = null,
    val description: String? = null,
    @SerializedName("sender_name")
    val senderName: String? = null,
    val end: Int? = null,
    val event: String? = null,
    val tags: List<String?>? = null
)

data class WeatherItem(
    val icon: String? = null,
    val description: String? = null,
    val main: String? = null,
    val id: Int? = null
)

data class Temp(
    val min: Double? = null,
    val max: Double? = null,
    val eve: Double? = null,
    val night: Double? = null,
    val day: Double? = null,
    val morn: Double? = null
)

data class HourlyItem(
    val temp: Double? = null,
    val visibility: Int? = null,
    val uvi: Double? = null,
    val pressure: Int? = null,
    val clouds: Int? = null,
    @SerializedName("feels_like")
    val feelsLike: Double? = null,
    @SerializedName("wind_gust")
    val windGust: Double? = null,
    val dt: Long? = null,
    val pop: Double? = null,
    @SerializedName("wind_deg")
    val windDeg: Int? = null,
    @SerializedName("dew_point")
    val dewPoint: Double? = null,
    val weather: List<WeatherItem?>? = null,
    val humidity: Int? = null,
    @SerializedName("wind_speed")
    val windSpeed: Double? = null
)

data class MinutelyItem(
    val dt: Long? = null,
    val precipitation: Int? = null
)

data class Current(
    val sunrise: Long? = null,
    val temp: Double? = null,
    val visibility: Int? = null,
    val uvi: Double? = null,
    val pressure: Int? = null,
    val clouds: Int? = null,
    @SerializedName("feels_like")
    val feelsLike: Double? = null,
    @SerializedName("wind_gust")
    val windGust: Double? = null,
    val dt: Long? = null,
    @SerializedName("wind_deg")
    val windDeg: Int? = null,
    @SerializedName("dew_point")
    val dewPoint: Double? = null,
    val sunset: Long? = null,
    val weather: List<WeatherItem?>? = null,
    val humidity: Int? = null,
    @SerializedName("wind_speed")
    val windSpeed: Double? = null
)

data class DailyItem(
    val moonset: Int? = null,
    val summary: String? = null,
    val sunrise: Int? = null,
    val temp: Temp? = null,
    @SerializedName("moon_phase")
    val moonPhase: Double? = null,
    val uvi: Double? = null,
    val moonrise: Int? = null,
    val pressure: Int? = null,
    val clouds: Int? = null,
    @SerializedName("feels_like")
    val feelsLike: FeelsLike? = null,
    @SerializedName("wind_gust")
    val windGust: Double? = null,
    val dt: Long? = null,
    val pop: Double? = null,
    @SerializedName("wind_deg")
    val windDeg: Int? = null,
    @SerializedName("dew_point")
    val dewPoint: Double? = null,
    val sunset: Int? = null,
    val weather: List<WeatherItem?>? = null,
    val humidity: Int? = null,
    @SerializedName("wind_speed")
    val windSpeed: Double? = null
)

data class FeelsLike(
    val eve: Double? = null,
    val night: Double? = null,
    val day: Double? = null,
    val morn: Double? = null
)