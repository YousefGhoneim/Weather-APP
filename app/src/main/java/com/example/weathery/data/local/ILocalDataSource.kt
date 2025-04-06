package com.example.weathery.data.local

import com.example.weathery.data.models.WeatherUiModel
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {

    // Favorites (Room)
    suspend fun insertCity(city: CityEntity)
    fun getAllCities(): Flow<List<CityEntity>>
    suspend fun deleteCity(city: CityEntity)

    // Alarms (Room)
    suspend fun insertAlarm(alarm: WeatherAlarmEntity): Int
    fun getAllAlarms(): Flow<List<WeatherAlarmEntity>>
    suspend fun deleteAlarmById(alarmId: Int)
    suspend fun getAlarmById(id: Int): WeatherAlarmEntity?
    suspend fun deleteAlarm(alarm: WeatherAlarmEntity)
    suspend fun deleteExpiredAlarms(currentTime: Long)

    // Cached Weather (Room)
    suspend fun saveCachedWeather(city: String, weather: WeatherUiModel)
    suspend fun getCachedWeather(): WeatherUiModel?

    // Settings (SharedPreferences)
    fun getLanguage(): String
    fun setLanguage(language: String)

    fun getUnitSystem(): String
    fun setUnitSystem(system: String)

    fun getLocationSource(): String
    fun setLocationSource(source: String)

    fun getPickedLocation(): Triple<Double, Double, String>?
    fun setPickedLocation(lat: Double, lon: Double, city: String)
}
