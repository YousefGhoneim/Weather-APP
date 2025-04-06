package com.example.weathery.data.repo

import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.models.OneResponse
import com.example.weathery.data.models.WeatherUiModel
import kotlinx.coroutines.flow.Flow

interface IWeatherRepository {

    //  Remote (API)
    suspend fun fetchWeather(lat: Double, lon: Double, units: String): OneResponse

    //  Favorites (Room)
    suspend fun saveCity(city: CityEntity)
    fun getFavoriteCities(): Flow<List<CityEntity>>
    suspend fun deleteCity(city: CityEntity)

    //  Alarms (Room)
    suspend fun saveAlarm(alarm: WeatherAlarmEntity): Int
    suspend fun getAllAlarms(): Flow<List<WeatherAlarmEntity>>
    suspend fun getAlarmById(id: Int): WeatherAlarmEntity?
    suspend fun deleteAlarm(alarm: WeatherAlarmEntity)
    suspend fun deleteAlarmById(alarmId: Int)
    suspend fun deleteExpiredAlarms(currentTime: Long)

    //  Cached weather (Room)
    suspend fun cacheWeather(city: String, weather: WeatherUiModel)
    suspend fun getCachedWeather(): WeatherUiModel?

    //  Settings (SharedPreferences)
    fun getLanguage(): String
    fun setLanguage(value: String)

    fun getUnitSystem(): String
    fun setUnitSystem(value: String)

    fun getLocationSource(): String
    fun setLocationSource(value: String)

    fun getPickedLocation(): Triple<Double, Double, String>?
    fun setPickedLocation(lat: Double, lon: Double, city: String)
}
