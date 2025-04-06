package com.example.weathery.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.weathery.utils.RepositoryProvider
import com.example.weathery.utils.toWeatherUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmDebug", "📡 AlarmReceiver received broadcast")

        val alarmId = intent.getIntExtra("alarm_id", -1)
        Log.d("AlarmDebug", "📦 Extracted alarmId=$alarmId from intent")

        if (alarmId == -1) {
            Log.e("AlarmDebug", "❌ Invalid alarm ID received")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = RepositoryProvider.provideRepository(context)
                val alarm = repo.getAlarmById(alarmId) ?: return@launch

                val unit = repo.getUnitSystem()
                val weather = repo.fetchWeather(alarm.lat, alarm.lon, unit).toWeatherUiModel(alarm.cityName, unit)

                val isMet = AlarmConditionChecker.isConditionMet(alarm, weather)

                if (isMet) {
                    Log.d("AlarmDebug", "🎯 Condition MET - triggering alarm!")
                    Log.d("AlarmDebug", "🎬 Trigger type: ${alarm.triggerType}")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "🔔 Alarm triggered for ${alarm.cityName}", Toast.LENGTH_SHORT).show()
                    }

                    when (alarm.triggerType) {
                        "alarm" -> {
                            val overlayIntent = Intent(context, AlarmOverlayActivity::class.java).apply {
                                putExtra("weather_condition", weather.current.main)
                                putExtra("country", weather.current.cityName)
                                putExtra("temperature", weather.current.temp.toString())
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            context.startActivity(overlayIntent)
                        }
                        "notification" -> {
                            NotificationHelper.showWeatherNotification(
                                context = context,
                                title = alarm.cityName,
                                message = weather.current.description,
                                lat = alarm.lat,
                                lon = alarm.lon

                            )
                        }
                    }

                    repo.deleteAlarm(alarm)
                    Log.d("AlarmDebug", "🗑️ Alarm auto-deleted after triggering")
                }

            } catch (e: Exception) {
                Log.e("AlarmDebug", "💥 Exception in AlarmReceiver: ${e.localizedMessage}", e)
            }
        }

    }
}
