package com.example.weathery

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed class ScreenRoute(val route: String, val icon: ImageVector, val label: String) {
    @Serializable object Home : ScreenRoute("home", Icons.Default.Home, "Home")
    @Serializable object Favorites : ScreenRoute("favorites", Icons.Default.Favorite, "Favorites")
    @Serializable object Alarm : ScreenRoute("alarm", Icons.Default.Notifications, "Alarm")
    @Serializable object Settings : ScreenRoute("settings", Icons.Default.Settings, "Settings")
}


