package com.example.weathery.data.repo

import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.models.OneResponse
import com.example.weathery.data.remote.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) {

    // 🔹 Remote (API)
    suspend fun fetchWeather(lat: Double, lon: Double): OneResponse {
        return remoteDataSource.getWeather(lat, lon)
    }

    // 🔹 Favorites
    suspend fun saveCity(city: CityEntity) = localDataSource.insertCity(city)
    fun getFavoriteCities(): Flow<List<CityEntity>> = localDataSource.getAllCities()
    suspend fun deleteCity(city: CityEntity) = localDataSource.deleteCity(city)

    // 🔹 Alarms
    suspend fun saveAlarm(alarm: WeatherAlarmEntity): Int {
        return localDataSource.insertAlarm(alarm)
    }

    suspend fun deleteExpiredAlarms(currentTime: Long) = localDataSource.deleteExpiredAlarms(currentTime)

    suspend fun getAllAlarms(): Flow<List<WeatherAlarmEntity>> = localDataSource.getAllAlarms()
    suspend fun getAlarmById(id: Int) = localDataSource.getAlarmById(id)
    suspend fun deleteAlarm(alarm: WeatherAlarmEntity) = localDataSource.deleteAlarm(alarm)
    suspend fun deleteAlarmById(alarmId: Int) = localDataSource.deleteAlarmById(alarmId)

    companion object {
        @Volatile private var INSTANCE: WeatherRepository? = null

        fun getInstance(
            remote: WeatherRemoteDataSource,
            local: WeatherLocalDataSource
        ): WeatherRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = WeatherRepository(remote, local)
                INSTANCE = instance
                instance
            }
        }
    }
}
