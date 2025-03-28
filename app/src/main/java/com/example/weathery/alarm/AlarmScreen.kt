package com.example.weathery.alarm

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.weathery.R
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.favourite.WeatherLottieBackground
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmsViewModel,
    navController: NavHostController,
    onPickLocationFromMap: (onLocationSelected: (Double, Double, String) -> Unit) -> Unit
) {
    val state by viewModel.alarmsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deletedAlarm by remember { mutableStateOf<WeatherAlarmEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(deletedAlarm) {
        deletedAlarm?.let { alarm ->
            val result = snackbarHostState.showSnackbar(
                message = "${alarm.cityName} removed",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.addAlarm(alarm)
            }
            deletedAlarm = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addAlarm") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { padding ->

        val alarms = if (state is UiState.Success<*>) {
            (state as UiState.Success<List<WeatherAlarmEntity>>).data
        } else emptyList()

        val bgCondition = if (alarms.isEmpty()) "clear" else "cloud"

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            AlarmLottieBackground(condition = bgCondition)

            // 🌫 Overlay color
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        if (alarms.isEmpty())
                            Color(0xFF0D1B2A).copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {

                Button(onClick = {
                    scope.launch {
                        val triggerTime = System.currentTimeMillis() + 10_000L // 10 seconds

                        // 1. Create a fake alarm entity
                        val testAlarm = WeatherAlarmEntity(
                            id = 0, // ID will be auto-generated
                            cityName = "Test City",
                            lat = 0.0,
                            lon = 0.0,
                            startTime = triggerTime,
                            endTime = triggerTime + 60_000,
                            conditionType = "",
                            condition = "",
                            thresholdValue = null,
                            triggerType = "notification" // or "alarm"
                        )

                        // 2. Save to Room and get the new ID
                        val alarmId = viewModel.saveAndReturnId(testAlarm)

                        // 3. Schedule the alarm
                        viewModel.scheduleAlarm(
                            context = context,
                            alarmTime = triggerTime,
                            requestCode = alarmId,
                            alarmId = alarmId
                        )

                        Toast.makeText(context, "Test alarm saved & scheduled in 10s", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("🔔 Test Alarm Now")
                }

                when (state) {
                    is UiState.Loading -> CircularProgressIndicator()
                    is UiState.Error -> Text("Error loading alarms")
                    is UiState.Empty -> Text("No alarms set")
                    is UiState.Success<*> -> {
                        LazyColumn {
                            items(alarms.size, key = { alarms[it].id }) { index ->
                                val alarm = alarms[index]
                                val dismissState = rememberSwipeToDismissBoxState(
                                    initialValue = SwipeToDismissBoxValue.Settled
                                )
                                val currentAlarm by rememberUpdatedState(alarm)

                                LaunchedEffect(dismissState.currentValue) {
                                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                        viewModel.deleteAlarm(currentAlarm)
                                        deletedAlarm = currentAlarm
                                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                    }
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {},
                                    content = {
                                        FancyAlarmCard(
                                            alarm = alarm,
                                            onDelete = {
                                                deletedAlarm = alarm
                                                viewModel.deleteAlarm(alarm)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FancyAlarmCard(
    alarm: WeatherAlarmEntity,
    onDelete: () -> Unit
) {
    val start = remember(alarm.startTime) {
        val cal = Calendar.getInstance().apply { timeInMillis = alarm.startTime }
        "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }
    val end = remember(alarm.endTime) {
        val cal = Calendar.getInstance().apply { timeInMillis = alarm.endTime }
        "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(2.dp, MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = alarm.cityName, style = MaterialTheme.typography.titleMedium)
                Text(text = "Time: $start → $end", style = MaterialTheme.typography.bodyMedium)
                if (alarm.conditionType.isNotEmpty()) {
                    val conditionDisplay = if (alarm.conditionType == "weather") {
                        alarm.condition
                    } else {
                        "${alarm.conditionType}: ${alarm.thresholdValue ?: "?"}"
                    }
                    Text("Condition: $conditionDisplay", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete alarm"
                )
            }
        }
    }
}

@Composable
fun AlarmLottieBackground(condition: String) {
    val animationRes = when {
        "clear" in condition.lowercase() -> R.raw.alarm
        "cloud" in condition.lowercase() -> R.raw.cloud
        "rain" in condition.lowercase() -> R.raw.rain
        "snow" in condition.lowercase() -> R.raw.snow
        else -> R.raw.cloud
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
