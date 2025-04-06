package com.example.weathery.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NetworkSnackbar(
    snackbarHostState: SnackbarHostState,
    connectionState: State<Boolean?>,
    context: Context = LocalContext.current
) {
    val isConnected = connectionState.value

    LaunchedEffect(isConnected) {
        if (isConnected == false) {
            val result = snackbarHostState.showSnackbar(
                message = "You're offline. Check your internet connection.",
                actionLabel = "Settings",
                duration = androidx.compose.material3.SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
    }

    // Host the snackbar UI (just in case nothing else hosts it)
    SnackbarHost(hostState = snackbarHostState)
}
