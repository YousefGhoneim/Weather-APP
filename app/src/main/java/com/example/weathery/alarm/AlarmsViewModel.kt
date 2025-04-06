package com.example.weathery.alarm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.data.repo.IWeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AlarmsViewModel(private val repository: IWeatherRepository) : ViewModel() {

    private val _alarmsState = MutableStateFlow<UiState<List<WeatherAlarmEntity>>>(UiState.Loading)
    val alarmsState: StateFlow<UiState<List<WeatherAlarmEntity>>> = _alarmsState

    private val _pickedLocation = MutableStateFlow<Triple<Double, Double, String>?>(null)
    val pickedLocation: StateFlow<Triple<Double, Double, String>?> = _pickedLocation

    init {
        Log.d("AlarmDebug", "🔄 Initializing ViewModel: Loading alarms...")
        loadAlarms()
    }

    fun setPickedAlarmLocation(lat: Double, lon: Double, city: String) {
        Log.d("AlarmDebug", "📍 Picked location set to $city ($lat, $lon)")
        _pickedLocation.value = Triple(lat, lon, city)
    }

    fun loadAlarms() {
        viewModelScope.launch {
            Log.d("AlarmDebug", "🔃 Loading all alarms from repository...")
            repository.deleteExpiredAlarms(System.currentTimeMillis())

            repository.getAllAlarms().collect { alarms ->
                _alarmsState.value = if (alarms.isEmpty()) {
                    Log.d("AlarmDebug", "📭 No alarms found.")
                    UiState.Empty
                } else {
                    Log.d("AlarmDebug", "📥 Loaded ${alarms.size} alarms.")
                    alarms.forEach { Log.d("AlarmDebug", "➡️ $it") }
                    UiState.Success(alarms)
                }
            }
        }
    }

    suspend fun saveAndReturnId(alarm: WeatherAlarmEntity): Int {
        Log.d("AlarmDebug", "💾 Saving alarm to DB (no ID yet): $alarm")
        val id = repository.saveAlarm(alarm)
        Log.d("AlarmDebug", "✅ Alarm saved with ID=$id")
        return id
    }

    fun cleanExpiredAlarms() {
        viewModelScope.launch {
            Log.d("AlarmDebug", "🧹 Cleaning expired alarms...")
            val now = System.currentTimeMillis()
            repository.deleteExpiredAlarms(now)
        }
    }

    fun addAlarm(alarm: WeatherAlarmEntity) {
        viewModelScope.launch {
            Log.d("AlarmDebug", "🔁 Re-adding alarm (Undo): $alarm")
            repository.saveAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: WeatherAlarmEntity) {
        viewModelScope.launch {
            Log.d("AlarmDebug", "🗑️ Deleting alarm with ID=${alarm.id}, city=${alarm.cityName}")
            repository.deleteAlarm(alarm)
        }
    }

    class Factory(private val repo: IWeatherRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlarmsViewModel(repo) as T
        }
    }
}
