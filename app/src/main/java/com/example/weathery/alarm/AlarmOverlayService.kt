package com.example.weathery.alarm

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.weathery.R
import com.example.weathery.ui.theme.WeatheryTheme

class AlarmOverlayActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    // Variables to hold the data passed from AlarmReceiver
    private lateinit var weatherCondition: String
    private lateinit var country: String
    private var temperature: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the data passed from the AlarmReceiver
        weatherCondition = intent.getStringExtra(getString(R.string.weather_conditionn)) ?: "Unknown"
        country = intent.getStringExtra(getString(R.string.country)) ?: "Unknown"
        temperature = intent.getStringExtra(getString(R.string.temperature)) ?: "--"

        // Set the window type and flags for overlay
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        // Play custom alarm sound from raw folder
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        setContent {
            WeatheryTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp) // Add padding around the card
                ) {
                    // Alarm card UI
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF192644),
                                        Color(0xFF112A59)
                                    ) // Dark gradient background
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.alarm), style = MaterialTheme.typography.headlineLarge, color = Color.White)

                            // Display actual Weather Condition and Country
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.weather, weatherCondition),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.countryy, country),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.temperature_c, temperature),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Buttons side by side with space in between
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Snooze Button
                                Button(
                                    onClick = {
                                        val snoozeTime = System.currentTimeMillis() + 5 * 60 * 1000
                                        AlarmScheduler.scheduleSnoozedAlarm(this@AlarmOverlayActivity, snoozeTime)
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50) // Green color for snooze
                                    ),
                                    modifier = Modifier.weight(1f) // Equal width
                                ) {
                                    Text(stringResource(R.string.snooze), color = Color.White)
                                }

                                // Dismiss Button
                                Button(
                                    onClick = {
                                        mediaPlayer?.stop()
                                        mediaPlayer?.release()
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF44336) // Red color for dismiss
                                    ),
                                    modifier = Modifier.weight(1f) // Equal width
                                ) {
                                    Text(stringResource(R.string.dismiss), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // Check if mediaPlayer is initialized and in a valid state before stopping or releasing it
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                }
            } catch (e: IllegalStateException) {
                Log.e("AlarmOverlayActivity", "Error while stopping MediaPlayer: ${e.localizedMessage}")
            } finally {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

}
