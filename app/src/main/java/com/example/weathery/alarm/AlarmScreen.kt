package com.example.weathery.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.weathery.R
import com.example.weathery.data.local.WeatherAlarmEntity
import com.example.weathery.data.models.UiState
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

    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter("com.example.weathery.ALARM_TRIGGERED"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(deletedAlarm) {
        deletedAlarm?.let { alarm ->
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.removedd, alarm.cityName),
                actionLabel = context.getString(R.string.undoo),
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.addAlarm(alarm)
            }
            deletedAlarm = null
        }
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text(stringResource(R.string.overlay_permission_required)) },
            text = { Text(stringResource(R.string.to_display_alarms_over_other_apps_please_allow_overlay_permission_in_settings)) },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayPermissionDialog = false
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!Settings.canDrawOverlays(context)) {
                        showOverlayPermissionDialog = true
                    } else {
                        navController.navigate("addAlarm")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_alarmm))
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

            // Overlay effect
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

                // Alarm List
                when (state) {
                    is UiState.Loading -> CircularProgressIndicator()
                    is UiState.Error -> Text("Error loading alarms")
                    is UiState.Empty -> Text("No alarms set")
                    is UiState.Success<*> -> {
                        LazyColumn {
                            items(alarms.size, key = { alarms[it].id }) { index ->
                                val alarm = alarms[index]
                                val dismissState = rememberSwipeToDismissBoxState()
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
                Text(text = stringResource(R.string.time, start, end), style = MaterialTheme.typography.bodyMedium)
                if (alarm.conditionType.isNotEmpty()) {
                    val conditionDisplay = if (alarm.conditionType == "weather") {
                        alarm.condition
                    } else {
                        "${alarm.conditionType}: ${alarm.thresholdValue ?: "?"}"
                    }
                    Text(stringResource(R.string.conditionn, conditionDisplay), style = MaterialTheme.typography.bodySmall)
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
