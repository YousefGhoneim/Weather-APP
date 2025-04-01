// SettingsScreen.kt (Final fix: handle permanently denied location permission)
package com.example.weathery.setting

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.weathery.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.weathery.home.WeatherLottieBackground
import com.example.weathery.utils.LocationHandler

@Composable
fun SettingsScreen(
    onRequestMapPicker: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context) }
    val handler = remember { LocationHandler(context as ComponentActivity) }

    val language by viewModel.language.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()
    val locationSource by viewModel.locationSource.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        WeatherLottieBackground("settings")

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.language))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = language == "device",
                        onClick = {
                            viewModel.setLanguage("device")
                            (context as? ComponentActivity)?.recreate()
                        }
                    )
                    Text(stringResource(R.string.device_language))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = language == "en",
                        onClick = {
                            viewModel.setLanguage("en")
                            (context as? ComponentActivity)?.recreate()
                        }
                    )
                    Text(stringResource(R.string.english))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = language == "ar",
                        onClick = {
                            viewModel.setLanguage("ar")
                            (context as? ComponentActivity)?.recreate()
                        }
                    )
                    Text(stringResource(R.string.arabic))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.measurement_system))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = unitSystem == "metric",
                    onClick = { viewModel.setUnitSystem("metric") }
                )
                Text(stringResource(R.string.metric))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = unitSystem == "imperial",
                    onClick = { viewModel.setUnitSystem("imperial") }
                )
                Text(stringResource(R.string.imperial))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = unitSystem == "standard",
                    onClick = { viewModel.setUnitSystem("standard") }
                )
                Text(stringResource(R.string.standard))
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.location_source))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = locationSource == "gps",
                    onClick = {
                        val activity = context as ComponentActivity
                        if (!handler.checkPermissions()) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                                Toast.makeText(context, "Location permission denied. Please enable it from app settings.", Toast.LENGTH_LONG).show()
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                })
                            } else {
                                handler.requestPermissions()
                            }
                        } else if (!handler.isLocationEnabled()) {
                            Toast.makeText(context, "Opening location settings...", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } else {
                            viewModel.setLocationSource("gps")
                            Toast.makeText(context, "Using GPS for location", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Text(stringResource(R.string.gps))

                RadioButton(
                    selected = locationSource == "map",
                    onClick = {
                        viewModel.setLocationSource("map")
                        onRequestMapPicker()
                    }
                )
                Text(stringResource(R.string.map_picker))
            }
        }
    }
}
