package com.example.weathery.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.weathery.R
import com.example.weathery.alarm.PreviewWeatherActivity

object NotificationHelper {

    private const val CHANNEL_ID = "weather_alerts_v2" // 🔄 NEW CHANNEL ID
    private const val CHANNEL_NAME = "Weather Alerts"

    fun showWeatherNotification(
        context: Context,
        title: String,
        message: String,
        lat: Double,
        lon: Double
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 🔔 Custom sound URI
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")

        // 🔗 Intent to open PreviewWeatherActivity with location data
        val intent = Intent(context, PreviewWeatherActivity::class.java).apply {
            putExtra("city", title)
            putExtra("lat", lat)
            putExtra("lon", lon)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 📢 Notification channel with custom sound (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggered alarms for weather conditions"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setSound(soundUri, attributes)
            }

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Weather Alert: $title")
            .setContentText(message)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }
}
