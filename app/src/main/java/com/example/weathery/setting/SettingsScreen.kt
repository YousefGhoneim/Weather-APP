package com.example.weathery.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context) }

    val language by viewModel.language.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val windSpeedUnit by viewModel.windSpeedUnit.collectAsState()
    val locationSource by viewModel.locationSource.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Language")
        Row {
            RadioButton(
                selected = language == "en",
                onClick = { viewModel.setLanguage("en") }
            )
            Text("English")
            RadioButton(
                selected = language == "ar",
                onClick = { viewModel.setLanguage("ar") }
            )
            Text("Arabic")
        }

        Spacer(Modifier.height(8.dp))
        Text("Temperature Unit")
        Row {
            RadioButton(
                selected = tempUnit == "metric",
                onClick = { viewModel.setTempUnit("metric") }
            )
            Text("Celsius")
            RadioButton(
                selected = tempUnit == "imperial",
                onClick = { viewModel.setTempUnit("imperial") }
            )
            Text("Fahrenheit")
        }

        Spacer(Modifier.height(8.dp))
        Text("Wind Speed Unit")
        Row {
            RadioButton(
                selected = windSpeedUnit == "m/s",
                onClick = { viewModel.setWindSpeedUnit("m/s") }
            )
            Text("m/s")
            RadioButton(
                selected = windSpeedUnit == "mph",
                onClick = { viewModel.setWindSpeedUnit("mph") }
            )
            Text("mph")
        }

        Spacer(Modifier.height(8.dp))
        Text("Location Source")
        Row {
            RadioButton(
                selected = locationSource == "gps",
                onClick = { viewModel.setLocationSource("gps") }
            )
            Text("GPS")
            RadioButton(
                selected = locationSource == "map",
                onClick = { viewModel.setLocationSource("map") }
            )
            Text("Map Picker")
        }
    }
}
