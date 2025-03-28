package com.example.weathery

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.weathery.ui.theme.WeatheryTheme
import com.example.weathery.utils.LocationHandler
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var locationHandler: LocationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show native splash screen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationHandler = LocationHandler(this)

        // Request location permission on app launch
        if (!locationHandler.checkPermissions()) {
            locationHandler.requestPermissions()
        }

        setContent {
            WeatheryTheme {
                var isLoading by remember { mutableStateOf(true) }
                var showOnboarding by remember { mutableStateOf(false) }

                // Simulate loading and check onboarding status
                LaunchedEffect(Unit) {
                    delay(1000) // splash delay
                    val prefs = getSharedPreferences("weathery_prefs", Context.MODE_PRIVATE)
                    showOnboarding = !prefs.getBoolean("onboarding_completed", false)
                    isLoading = false
                }

                splashScreen.setKeepOnScreenCondition { isLoading }

                if (isLoading) return@WeatheryTheme

                if (showOnboarding) {
                    OnboardingScreen(
                        onDone = {
                            // Save preference and go to main
                            getSharedPreferences("weathery_prefs", Context.MODE_PRIVATE)
                                .edit().putBoolean("onboarding_completed", true).apply()
                            showOnboarding = false
                        }
                    )
                } else {
                    MainNavigation()
                }
            }
        }
    }
}
