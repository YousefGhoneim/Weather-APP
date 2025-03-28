package com.example.weathery.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionDialog(
    context: Context,
    onDismiss: () -> Unit,
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) showDialog = true
            else onGranted()
        } else {
            onGranted() // Pre-Tiramisu doesn't need runtime permission
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()
            },
            title = { Text("Enable Notifications") },
            text = {
                Text("This app needs permission to send notifications, like weather alarms. Allow it?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (context is ComponentActivity) {
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1010
                        )
                    }
                    onGranted()
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDenied()
                }) {
                    Text("No Thanks")
                }
            }
        )
    }
}