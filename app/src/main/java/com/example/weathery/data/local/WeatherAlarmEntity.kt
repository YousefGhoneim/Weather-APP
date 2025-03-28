package com.example.weathery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_alarms")
data class WeatherAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val lat: Double,
    val lon: Double,
    val startTime: Long,
    val endTime: Long,
    val condition: String = "",
    val conditionType: String = "",
    val thresholdValue: Double? = null,
    val triggerType: String = "notification"
)

