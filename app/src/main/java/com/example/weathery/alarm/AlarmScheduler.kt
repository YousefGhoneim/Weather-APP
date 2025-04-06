package com.example.weathery.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.weathery.data.local.WeatherAlarmEntity
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context, alarm: WeatherAlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmDebug", "Scheduling alarm ID=${alarm.id}")
        Log.d("AlarmDebug", " Time: ${formatTime(alarm.startTime)} (${alarm.startTime})")
        Log.d("AlarmDebug", " Location: ${alarm.cityName} (${alarm.lat}, ${alarm.lon})")
        Log.d("AlarmDebug", " Trigger: ${alarm.triggerType}, ConditionType=${alarm.conditionType}, Condition=${alarm.condition}, Threshold=${alarm.thresholdValue}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarm.startTime,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AlarmDebug", "❌ Canceled alarm with ID=$alarmId")
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleSnoozedAlarm(context: Context, snoozeTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", Int.MAX_VALUE)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Int.MAX_VALUE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmDebug", " Snoozed alarm scheduled for ${formatTime(snoozeTime)}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }
}
