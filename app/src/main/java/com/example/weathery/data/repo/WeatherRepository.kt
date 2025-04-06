package com.example.weathery.data.repo

import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.local.ILocalDataSource
import com.example.weathery.data.models.OneResponse
import com.example.weathery.data.models.WeatherUiModel
import com.example.weathery.data.remote.IRemoteDataSource
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val remote: IRemoteDataSource,
    private val local: ILocalDataSource
) : IWeatherRepository {

    // Remote
    override suspend fun fetchWeather(lat: Double, lon: Double, units: String): OneResponse {
        return remote.getWeather(lat, lon, units)
    }

    // Favorites
    override suspend fun saveCity(city: CityEntity) = local.insertCity(city)
    override fun getFavoriteCities(): Flow<List<CityEntity>> = local.getAllCities()
    override suspend fun deleteCity(city: CityEntity) = local.deleteCity(city)

    // Alarms
    override suspend fun saveAlarm(alarm: WeatherAlarmEntity): Int = local.insertAlarm(alarm)
    override suspend fun getAllAlarms(): Flow<List<WeatherAlarmEntity>> = local.getAllAlarms()
    override suspend fun getAlarmById(id: Int): WeatherAlarmEntity? = local.getAlarmById(id)
    override suspend fun deleteAlarm(alarm: WeatherAlarmEntity) = local.deleteAlarm(alarm)
    override suspend fun deleteAlarmById(alarmId: Int) = local.deleteAlarmById(alarmId)
    override suspend fun deleteExpiredAlarms(currentTime: Long) = local.deleteExpiredAlarms(currentTime)

    // Cached Weather
    override suspend fun cacheWeather(city: String, weather: WeatherUiModel) =
        local.saveCachedWeather(city, weather)

    override suspend fun getCachedWeather(): WeatherUiModel? = local.getCachedWeather()

    // Settings
    override fun getLanguage(): String = local.getLanguage()
    override fun setLanguage(value: String) = local.setLanguage(value)

    override fun getUnitSystem(): String = local.getUnitSystem()
    override fun setUnitSystem(value: String) = local.setUnitSystem(value)

    override fun getLocationSource(): String = local.getLocationSource()
    override fun setLocationSource(value: String) = local.setLocationSource(value)

    override fun getPickedLocation(): Triple<Double, Double, String>? = local.getPickedLocation()
    override fun setPickedLocation(lat: Double, lon: Double, city: String) =
        local.setPickedLocation(lat, lon, city)
}
