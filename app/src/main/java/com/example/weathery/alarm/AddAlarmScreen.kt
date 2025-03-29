
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
    existingAlarm: WeatherAlarmEntity? = null,
    onBack: () -> Unit,
    onPickLocationFromMap: () -> Unit,
    onSave: (WeatherAlarmEntity) -> Unit,
    viewModel: AlarmsViewModel
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showPermissionDialog = true
            }
        }
    }

    // 🔔 Notification permission dialog for Android 13+
    NotificationPermissionDialog(
        context = context,
        onDismiss = {},
        onGranted = {
            Log.d("NotificationPermission", "Granted from alarm screen")
        },
        onDenied = {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    )

    var city by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }

    var startTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var endTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let { (it + 5) % 7 } // Sunday = 0
    val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var selectedDays by remember { mutableStateOf(setOf(todayIndex)) }

    var conditionType by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }
    var timeValidationError by remember { mutableStateOf(false) }
    var pastTimeError by remember { mutableStateOf(false) }

    val conditionTypes = listOf("", "weather", "temp", "uvi")
    val weatherOptions = listOf("Clear", "Clouds", "Rain", "Snow", "Thunderstorm", "Drizzle", "Mist")

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            lat = it.first
            lon = it.second
            city = it.third
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AlarmAddedLottieBackground("clear")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(0.9f)
                    .height(420.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                val coroutineScope = rememberCoroutineScope()

                //Expired alarm cleanup every minute
                DisposableEffect(Unit) {
                    val handler = Handler(Looper.getMainLooper())
                    val checkRunnable = object : Runnable {
                        override fun run() {
                            viewModel.cleanExpiredAlarms()
                            handler.postDelayed(this, 60_000L)
                        }
                    }
                    handler.post(checkRunnable)
                    onDispose { handler.removeCallbacks(checkRunnable) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text("Add Alarm", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))

                    Text("Picked Location: ${if (city.isNotEmpty()) city else "None"}")
                    if (showErrors && (lat == null || lon == null)) {
                        Text("Please select a location", color = Color.Red)
                    }

                    Button(onClick = onPickLocationFromMap, modifier = Modifier.fillMaxWidth()) {
                        Text(if (city.isNotEmpty()) "Change Location" else "Pick Location")
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showStartPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Start Time: ${startTime?.formatTime() ?: "--:--"}")
                    }
                    Button(onClick = { showEndPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("End Time: ${endTime?.formatTime() ?: "--:--"}")
                    }
                    if (showErrors && (startTime == null || endTime == null)) {
                        Text("Please select both start and end time", color = Color.Red)
                    }
                    if (timeValidationError) {
                        Text("End time must be after start time", color = Color.Red)
                    }
                    if (pastTimeError) {
                        Text("Selected time must be in the future", color = Color.Red)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Repeat on Days:")
                    Column {
                        weekDays.forEachIndexed { index, day ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = index in selectedDays,
                                        onValueChange = {
                                            selectedDays = selectedDays.toMutableSet().apply {
                                                if (contains(index)) remove(index) else add(index)
                                            }
                                        }
                                    )
                                    .padding(start = 4.dp)
                            ) {
                                Checkbox(checked = index in selectedDays, onCheckedChange = null)
                                Text(day)
                            }
                        }
                    }
                    if (showErrors && selectedDays.isEmpty()) {
                        Text("Select at least one day", color = Color.Red)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Condition Type")
                    conditionTypes.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = conditionType == it,
                                onClick = { conditionType = it; condition = ""; threshold = "" }
                            )
                            Text(it.ifEmpty { "None" })
                        }
                    }

                    if (conditionType == "weather") {
                        DropdownMenuContent(weatherOptions, condition) { condition = it }
                    }

                    if (conditionType == "temp" || conditionType == "uvi") {
                        OutlinedTextField(
                            value = threshold,
                            onValueChange = { threshold = it },
                            label = { Text("Threshold Value") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    var triggerType by remember { mutableStateOf("notification") }
                    val triggerOptions = listOf("notification", "alarm")
                    Text("Trigger Type")
                    triggerOptions.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = triggerType == it, onClick = { triggerType = it })
                            Text(it.replaceFirstChar { c -> c.uppercaseChar() })
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = onBack) { Text("Cancel") }
                        Button(
                            onClick = {
                                showErrors = true
                                timeValidationError = false
                                pastTimeError = false

                                if (lat != null && lon != null && startTime != null && endTime != null && selectedDays.isNotEmpty()) {
                                    val now = Calendar.getInstance()
                                    val todayIndex = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7
                                    val startTotalMinutes = startTime!!.first * 60 + startTime!!.second
                                    val endTotalMinutes = endTime!!.first * 60 + endTime!!.second

                                    if (endTotalMinutes <= startTotalMinutes) {
                                        timeValidationError = true
                                        return@Button
                                    }

                                    selectedDays.forEach { dayIndex ->
                                        val cal = Calendar.getInstance().apply {
                                            set(Calendar.DAY_OF_WEEK, (dayIndex + 1) % 7 + 1)
                                            set(Calendar.HOUR_OF_DAY, startTime!!.first)
                                            set(Calendar.MINUTE, startTime!!.second)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }

                                        if (dayIndex == todayIndex && cal.before(now)) {
                                            pastTimeError = true
                                            return@Button
                                        }

                                        if (cal.timeInMillis <= System.currentTimeMillis()) {
                                            cal.add(Calendar.WEEK_OF_YEAR, 1)
                                        }

                                        val calEnd = Calendar.getInstance().apply {
                                            timeInMillis = cal.timeInMillis
                                            set(Calendar.HOUR_OF_DAY, endTime!!.first)
                                            set(Calendar.MINUTE, endTime!!.second)
                                        }

                                        if (triggerType == "alarm" && !Settings.canDrawOverlays(context)) {
                                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                            Toast.makeText(context, "Please grant 'Draw over other apps' permission", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }

                                        val alarm = WeatherAlarmEntity(
                                            cityName = city,
                                            lat = lat!!,
                                            lon = lon!!,
                                            startTime = cal.timeInMillis,
                                            endTime = calEnd.timeInMillis,
                                            conditionType = conditionType,
                                            condition = condition,
                                            thresholdValue = threshold.toDoubleOrNull(),
                                            triggerType = triggerType
                                        )

                                        coroutineScope.launch {
                                            val alarmId = viewModel.saveAndReturnId(alarm)
                                            viewModel.scheduleAlarm(
                                                context = context,
                                                alarmTime = cal.timeInMillis,
                                                requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                                                alarmId = alarmId
                                            )
                                            Toast.makeText(context, "Alarms scheduled", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    }

                                    Toast.makeText(context, "Alarms scheduled", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            },
                            enabled = startTime != null && endTime != null
                        ) {
                            Text("Save")
                        }
                    }
                }
            }




        }
    }

    if (showStartPicker) {
        TimePickerDialog(context, { _, h, m ->
            startTime = h to m
            showStartPicker = false
        }, 8, 0, true).show()
    }
    if (showEndPicker) {
        TimePickerDialog(context, { _, h, m ->
            endTime = h to m
            showEndPicker = false
        }, 10, 0, true).show()
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Please allow exact alarm permission in settings.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("No")
                }
            }
        )
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
            label = { Text("Weather Condition") },
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
