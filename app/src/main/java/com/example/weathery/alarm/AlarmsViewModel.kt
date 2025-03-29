package com.example.weathery.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.data.repo.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.util.Log

class AlarmsViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _alarmsState = MutableStateFlow<UiState<List<WeatherAlarmEntity>>>(UiState.Loading)
    val alarmsState: StateFlow<UiState<List<WeatherAlarmEntity>>> = _alarmsState

    init {
        loadAlarms()
    }

    fun loadAlarms() {
        viewModelScope.launch {
            repository.deleteExpiredAlarms(System.currentTimeMillis())
            repository.getAllAlarms().collect { alarms ->
                _alarmsState.value = when {
                    alarms.isEmpty() -> UiState.Empty
                    else -> UiState.Success(alarms)
                }
            }
        }
    }

    suspend fun saveAndReturnId(alarm: WeatherAlarmEntity): Int {
        return repository.saveAlarm(alarm)
    }

    fun cleanExpiredAlarms() {
        viewModelScope.launch {
            repository.deleteExpiredAlarms(System.currentTimeMillis())
        }
    }

    fun addAlarm(alarm: WeatherAlarmEntity) {
        viewModelScope.launch {
            repository.saveAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: WeatherAlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun scheduleAlarm(context: Context, alarmTime: Long, requestCode: Int, alarmId: Int) {
        AlarmScheduler.scheduleAlarm(context, alarmTime, requestCode, alarmId)
    }

    class AlarmsViewModelFactory(
        private val repository: WeatherRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlarmsViewModel::class.java)) {
                return AlarmsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
