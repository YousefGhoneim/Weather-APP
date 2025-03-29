package com.example.weathery.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // 🔹 Favorites
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Query("SELECT * FROM favorites")
    fun getAllCities(): Flow<List<CityEntity>>

    @Delete
    suspend fun deleteCity(city: CityEntity): Int

    // 🔹 Alarms
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: WeatherAlarmEntity): Long

    @Query("SELECT * FROM weather_alarms")
    fun getAllAlarms(): Flow<List<WeatherAlarmEntity>>

    @Delete
    suspend fun deleteAlarm(alarm: WeatherAlarmEntity): Int

    @Query("DELETE FROM weather_alarms WHERE endTime < :currentTime")
    suspend fun deleteExpiredAlarms(currentTime: Long)


    @Query("SELECT * FROM weather_alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): WeatherAlarmEntity?

    @Query("DELETE FROM weather_alarms WHERE id = :alarmId")
    suspend fun deleteAlarmById(alarmId: Int): Int
}
