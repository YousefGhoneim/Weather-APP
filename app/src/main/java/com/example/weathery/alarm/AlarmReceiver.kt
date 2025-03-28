package com.example.weathery.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
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
                Log.d("AlarmReceiver", "Received alarm for: ${alarm.cityName}, triggerType: ${alarm.triggerType}")

                if (alarm.triggerType == "alarm") {
                    playAlarmSound(context)
                } else {
                    showNotification(context, alarm.cityName , alarmId)
                }
            } else {
                Log.e("AlarmReceiver", "No alarm found with ID: $alarmId")
            }
        }
    }

    private fun showNotification(context: Context, cityName: String, alarmId: Int) {
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏰ Weather Alarm")
            .setContentText("Alarm for $cityName triggered.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri) // 👈 Custom sound
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alarmId, notification)

        // Optional: Toast to confirm
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Notification for $cityName", Toast.LENGTH_SHORT).show()
        }
    }


    private fun playAlarmSound(context: Context) {
        val alarmUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")
        val ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()
    }

}
