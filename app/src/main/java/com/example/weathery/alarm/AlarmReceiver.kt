package com.example.weathery.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.weathery.R
import com.example.weathery.data.local.WeatherDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.media.AudioAttributes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.weathery.MainActivity
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.models.UiState
import com.example.weathery.data.remote.RetrofitHelper
import com.example.weathery.data.remote.WeatherApiService
import com.example.weathery.data.remote.WeatherRemoteDataSource
import com.example.weathery.data.repo.WeatherRepository
import com.example.weathery.home.WeatherViewModel
import com.example.weathery.PreviewWeatherScreen


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Triggered at ${System.currentTimeMillis()}")

        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId == -1) {
            Log.e("AlarmReceiver", "Invalid alarm_id received")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = WeatherDatabase.getInstance(context)
            val alarm = db.weatherDao().getAlarmById(alarmId)

            if (alarm != null) {
                Log.d(
                    "AlarmReceiver",
                    "Received alarm for: ${alarm.cityName}, triggerType: ${alarm.triggerType}"
                )

                // Initialize ViewModel
                val weatherRepository = WeatherRepository.getInstance(
                    WeatherRemoteDataSource(RetrofitHelper.service),
                    WeatherLocalDataSource(db.weatherDao())
                )

                val viewModel = WeatherViewModel(weatherRepository)

                // Fetch the weather for the city's lat, lon
                viewModel.fetchWeather(alarm.lat, alarm.lon, alarm.cityName)

                // Collect the data from the ViewModel's StateFlow
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is UiState.Success -> {
                            val weatherData = uiState.data
                            val weatherCondition = weatherData.current.description
                            val temperature = weatherData.current.temp
                            val uvi = weatherData.extra.uvi
                            val country = alarm.cityName

                            // Check if the condition is met
                            val isConditionMet =
                                checkCondition(alarm, weatherCondition, temperature, uvi)

                            if (isConditionMet) {
                                // If the condition is met, proceed to handle the alarm based on its type
                                if (alarm.triggerType == "alarm") {
                                    // Start the AlarmOverlayActivity
                                    val activityIntent =
                                        Intent(context, AlarmOverlayActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra("weather_condition", weatherCondition)
                                            putExtra("temperature", temperature.toString())
                                            putExtra("country", country)
                                            putExtra("alarm_id", alarmId)
                                        }
                                    context.startActivity(activityIntent)


                                } else {
                                    // Show a notification instead of the overlay activity
                                    showNotification(
                                        context,
                                        weatherCondition,
                                        temperature.toString(),
                                        country,
                                        alarmId,
                                        alarm.lat,
                                        alarm.lon
                                    )
                                }

                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({
                                    // Delete alarm after the specified time
                                    CoroutineScope(Dispatchers.IO).launch {
                                        weatherRepository.deleteAlarmById(alarmId)
                                        Log.d("AlarmReceiver", "Alarm $alarmId automatically removed after trigger.")
                                    }
                                },  1000)
                            } else {
                                Log.d("AlarmReceiver", "Condition not met. Alarm will not trigger.")
                            }
                        }

                        is UiState.Error -> {
                            // Show error message on the main thread
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "Error fetching weather data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        else -> { /* Handle Loading State */
                        }
                    }
                }
            } else {
                Log.e("AlarmReceiver", "No alarm found with ID: $alarmId")
            }
        }
    }

    private fun checkCondition(
        alarm: WeatherAlarmEntity,
        weatherCondition: String,
        temperature: Int,
        uvi: Double?
    ): Boolean {
        return when (alarm.conditionType) {
            "temp" -> {
                val threshold = alarm.thresholdValue ?: return false
                temperature > threshold
            }

            "weather" -> {
                weatherCondition.equals(alarm.condition, ignoreCase = true)
            }

            "uvi" -> {
                val threshold = alarm.thresholdValue ?: return false
                uvi != null && uvi > threshold
            }

            else -> {
                true
            }
        }
    }


    private fun showNotification(context: Context, weatherCondition: String, temperature: String,cityName: String, alarmId: Int, lat: Double, lon: Double) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "weather_alarm_channel"

        // 🎵 Custom sound from res/raw
        val soundUri: Uri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")

        Log.d("AlarmReceiver", "showNotification:  $cityName")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Weather Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, attributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open the PreviewWeatherScreen when the notification is clicked
        val intent = Intent(context, PreviewWeatherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("city", cityName)
            putExtra("lat", lat)
            putExtra("lon", lon)
            putExtra("weather_condition", weatherCondition)
            putExtra("alarm_id", alarmId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or  PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏰ Weather Alarm")
            .setContentText("Alarm triggered for $cityName\nWeather: $weatherCondition\nTemperature: $temperature°C")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri) // Custom sound
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent to open the PreviewWeatherScreen
            .build()

        notificationManager.notify(alarmId, notification)

        // Optional: Toast to confirm
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Notification for $cityName", Toast.LENGTH_SHORT).show()
        }
    }
}

    private fun playAlarmSound(context: Context) {
        val alarmUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")
        val ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()
    }

