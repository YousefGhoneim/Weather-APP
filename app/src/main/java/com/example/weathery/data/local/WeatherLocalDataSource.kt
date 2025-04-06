package com.example.weathery.data.local

import com.example.weathery.data.models.WeatherUiModel
import com.example.weathery.data.local.WeatherDao
import com.example.weathery.data.local.CachedWeatherEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(
    private val dao: WeatherDao,
    private val prefs: SettingsPreferences
) : ILocalDataSource {

    // --- Favorites ---
    override suspend fun insertCity(city: CityEntity) {
        dao.insertCity(city)
    }

    override fun getAllCities(): Flow<List<CityEntity>> {
        return dao.getAllCities()
    }

    override suspend fun deleteCity(city: CityEntity) {
        dao.deleteCity(city)
    }

    // --- Alarms ---
    override suspend fun insertAlarm(alarm: WeatherAlarmEntity): Int {
        return dao.insertAlarm(alarm).toInt()
    }

    override fun getAllAlarms(): Flow<List<WeatherAlarmEntity>> {
        return dao.getAllAlarms()
    }

    override suspend fun deleteAlarmById(alarmId: Int) {
        dao.deleteAlarmById(alarmId)
    }

    override suspend fun getAlarmById(id: Int): WeatherAlarmEntity? {
        return dao.getAlarmById(id)
    }

    override suspend fun deleteAlarm(alarm: WeatherAlarmEntity) {
        dao.deleteAlarm(alarm)
    }

    override suspend fun deleteExpiredAlarms(currentTime: Long) {
        dao.deleteExpiredAlarms(currentTime)
    }

    // --- Cached Weather ---
    override suspend fun saveCachedWeather(city: String, weather: WeatherUiModel) {
        val json = Gson().toJson(weather)
        dao.insertCachedWeather(CachedWeatherEntity(cityName = city, dataJson = json))
    }

    override suspend fun getCachedWeather(): WeatherUiModel? {
        return dao.getCachedWeather()?.let {
            Gson().fromJson(it.dataJson, WeatherUiModel::class.java)
        }
    }

    // --- Settings (SharedPreferences) ---
    override fun getLanguage(): String = prefs.getLanguage()
    override fun setLanguage(language: String) = prefs.setLanguage(language)

    override fun getUnitSystem(): String = prefs.getUnitSystem()
    override fun setUnitSystem(system: String) = prefs.setUnitSystem(system)

    override fun getLocationSource(): String = prefs.getLocationSource()
    override fun setLocationSource(source: String) = prefs.setLocationSource(source)

    override fun getPickedLocation(): Triple<Double, Double, String>? = prefs.getPickedLocation()
    override fun setPickedLocation(lat: Double, lon: Double, city: String) =
        prefs.setPickedLocation(lat, lon, city)
}
