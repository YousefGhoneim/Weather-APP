package com.example.weathery.data.local

import com.example.weathery.data.models.WeatherUiModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(private val dao: WeatherDao) {

    //  Favorites
    suspend fun insertCity(city: CityEntity) = dao.insertCity(city)
    fun getAllCities(): Flow<List<CityEntity>> = dao.getAllCities()
    suspend fun deleteCity(city: CityEntity) = dao.deleteCity(city)

    //  Alarms
    suspend fun insertAlarm(alarm: WeatherAlarmEntity): Int {
        return dao.insertAlarm(alarm).toInt()
    }
    suspend fun deleteExpiredAlarms(currentTime: Long) = dao.deleteExpiredAlarms(currentTime)
    suspend fun deleteAlarmById(alarmId: Int) = dao.deleteAlarmById(alarmId)


    fun getAllAlarms(): Flow<List<WeatherAlarmEntity>> = dao.getAllAlarms()
    suspend fun getAlarmById(id: Int) = dao.getAlarmById(id)
    suspend fun deleteAlarm(alarm: WeatherAlarmEntity) = dao.deleteAlarm(alarm)


    // cash
    suspend fun saveCachedWeather(city: String, weather: WeatherUiModel) {
        val json = Gson().toJson(weather)
        dao.insertCachedWeather(CachedWeatherEntity(cityName = city, dataJson = json))
    }

    suspend fun getCachedWeather(): WeatherUiModel? {
        return dao.getCachedWeather()?.let {
            Gson().fromJson(it.dataJson, WeatherUiModel::class.java)
        }
    }

}
