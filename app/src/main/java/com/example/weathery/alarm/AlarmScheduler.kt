package com.example.weathery.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.local.WeatherDatabase
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.remote.RetrofitHelper
import com.example.weathery.data.remote.WeatherRemoteDataSource
import com.example.weathery.data.repo.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AlarmScheduler {

    fun scheduleSnoozedAlarm(context: Context, triggerAtMillis: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = WeatherDatabase.getInstance(context)
            val dao = db.weatherDao()
            val local = WeatherLocalDataSource(dao)
            val remote = WeatherRemoteDataSource(RetrofitHelper.service)
            val repo = WeatherRepository.getInstance(remote, local)

            val alarm = WeatherAlarmEntity(
                cityName = "Snoozed Alarm",
                lat = 0.0,
                lon = 0.0,
                startTime = triggerAtMillis,
                endTime = triggerAtMillis + 60_000,
                conditionType = "",
                condition = "",
                thresholdValue = null,
                triggerType = "alarm"
            )

            val alarmId = repo.saveAlarm(alarm)

            withContext(Dispatchers.Main) {
                scheduleAlarm(context, triggerAtMillis, alarmId, alarmId)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context, alarmTime: Long, requestCode: Int, alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )

        Log.d("AlarmScheduler", "Alarm scheduled at: $alarmTime with ID: $alarmId")
    }
}
