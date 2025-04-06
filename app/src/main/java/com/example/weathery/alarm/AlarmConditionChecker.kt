package com.example.weathery.alarm

import android.util.Log
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.models.WeatherUiModel

object AlarmConditionChecker {

    fun isConditionMet(alarm: WeatherAlarmEntity, weather: WeatherUiModel): Boolean {
        if (alarm.conditionType.isBlank()) {
            Log.d("AlarmDebug", "No condition set. Triggering alarm by default.")
            return true
        }

        return when (alarm.conditionType) {
            "weather" -> alarm.condition.equals(weather.current.main, ignoreCase = true)

            "temp_above" -> {
                val threshold = alarm.thresholdValue ?: return false
                weather.current.temp > threshold
            }

            "temp_below" -> {
                val threshold = alarm.thresholdValue ?: return false
                weather.current.temp < threshold
            }

            "wind_above" -> {
                val threshold = alarm.thresholdValue ?: return false
                weather.extra.windSpeed > threshold
            }

            "wind_below" -> {
                val threshold = alarm.thresholdValue ?: return false
                weather.extra.windSpeed < threshold
            }

            else -> false
        }
    }

}
