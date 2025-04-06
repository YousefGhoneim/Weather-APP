
package com.example.weathery.alarm

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.weathery.R
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.utils.NotificationPermissionDialog
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    selectedLocation: Triple<Double, Double, String>? = null,
    onBack: () -> Unit,
    onPickLocationFromMap: () -> Unit,
    viewModel: AlarmsViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var city by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }

    var startTimeMillis by remember { mutableStateOf<Long?>(null) }
    var endTimeMillis by remember { mutableStateOf<Long?>(null) }
    val startTimeLabel = remember { mutableStateOf("Pick Start Time") }
    val endTimeLabel = remember { mutableStateOf("Pick End Time") }

    var conditionType by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }

    var timeValidationError by remember { mutableStateOf(false) }
    var pastTimeError by remember { mutableStateOf(false) }

    var triggerType by remember { mutableStateOf("notification") }

    fun showDateTimePicker(label: MutableState<String>, onPicked: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(context, { _, h, min ->
                    calendar.set(Calendar.HOUR_OF_DAY, h)
                    calendar.set(Calendar.MINUTE, min)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onPicked(calendar.timeInMillis)
                    label.value = "$d/${m + 1}/$y ${"%02d:%02d".format(h, min)}"
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            lat = it.first
            lon = it.second
            city = it.third
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AlarmAddedLottieBackground("clear")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.add_alarm), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            Button(onClick = onPickLocationFromMap, modifier = Modifier.fillMaxWidth()) {
                Text(if (city.isNotEmpty()) stringResource(R.string.change_location) else stringResource(
                    R.string.pick_location
                )
                )
            }

            Spacer(Modifier.height(12.dp))
            if (city.isNotEmpty()) {
                Text(stringResource(R.string.selected_location, city), style = MaterialTheme.typography.bodyLarge)
            }


            Button(onClick = { showDateTimePicker(startTimeLabel) { startTimeMillis = it } }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.start, startTimeLabel.value))
            }
            Button(onClick = { showDateTimePicker(endTimeLabel) { endTimeMillis = it } }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.end, endTimeLabel.value))
            }

            if (timeValidationError) Text(stringResource(R.string.end_time_must_be_after_start_time), color = MaterialTheme.colorScheme.error)
            if (pastTimeError) Text(stringResource(R.string.time_must_be_in_the_future), color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(12.dp))

            Text(stringResource(R.string.condition_type))
            listOf("", "weather", "temp", "uvi").forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = conditionType == type, onClick = { conditionType = type })
                    Text(type.ifEmpty { stringResource(R.string.none) })
                }
            }

            if (conditionType == "weather") {
                OutlinedTextField(value = condition, onValueChange = { condition = it }, label = { Text(
                    stringResource(R.string.condition)
                ) })
            }

            if (conditionType == "temp" || conditionType == "uvi") {
                OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text(
                    stringResource(R.string.threshold)
                ) })
            }

            Spacer(Modifier.height(12.dp))

            Text(stringResource(R.string.trigger_type))
            listOf("notification", "alarm").forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = triggerType == type, onClick = { triggerType = type })
                    Text(type.replaceFirstChar { it.uppercaseChar() })
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = {
                val now = System.currentTimeMillis()
                if (startTimeMillis == null || endTimeMillis == null || startTimeMillis!! <= now || endTimeMillis!! <= startTimeMillis!!) {
                    timeValidationError = endTimeMillis != null && endTimeMillis!! <= startTimeMillis!!
                    pastTimeError = startTimeMillis != null && startTimeMillis!! <= now
                    return@Button
                }

                val alarm = WeatherAlarmEntity(
                    cityName = city,
                    lat = lat!!,
                    lon = lon!!,
                    startTime = startTimeMillis!!,
                    endTime = endTimeMillis!!,
                    conditionType = conditionType,
                    condition = condition,
                    thresholdValue = threshold.toDoubleOrNull(),
                    triggerType = triggerType
                )

                coroutineScope.launch {
                    val id = viewModel.saveAndReturnId(alarm)
                    AlarmScheduler.scheduleAlarm(context, alarm.copy(id = id))
                    Toast.makeText(context,
                        context.getString(R.string.alarm_scheduled), Toast.LENGTH_SHORT).show()
                    onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.save_alarm))
            }
        }
    }
}


private fun Pair<Int, Int>.formatTime(): String = "%02d:%02d".format(first, second)

@Composable
fun DropdownMenuContent(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.weather_condition)) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelected(it)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun AlarmAddedLottieBackground(condition: String) {
    val res = when {
        "clear" in condition.lowercase() -> R.raw.cloud
        "rain" in condition.lowercase() -> R.raw.rain
        "snow" in condition.lowercase() -> R.raw.snow
        else -> R.raw.cloud
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(res))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    LottieAnimation(composition, { progress }, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
}
