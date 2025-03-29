package com.example.weathery.data.local

import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(private val dao: WeatherDao) {

    // 🔸 Favorites
    suspend fun insertCity(city: CityEntity) = dao.insertCity(city)
    fun getAllCities(): Flow<List<CityEntity>> = dao.getAllCities()
    suspend fun deleteCity(city: CityEntity) = dao.deleteCity(city)

    // 🔸 Alarms
    suspend fun insertAlarm(alarm: WeatherAlarmEntity): Int {
        return dao.insertAlarm(alarm).toInt()
    }
    suspend fun deleteExpiredAlarms(currentTime: Long) = dao.deleteExpiredAlarms(currentTime)
    suspend fun deleteAlarmById(alarmId: Int) = dao.deleteAlarmById(alarmId)


    fun getAllAlarms(): Flow<List<WeatherAlarmEntity>> = dao.getAllAlarms()
    suspend fun getAlarmById(id: Int) = dao.getAlarmById(id)
    suspend fun deleteAlarm(alarm: WeatherAlarmEntity) = dao.deleteAlarm(alarm)
}
