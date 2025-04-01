package com.example.weathery

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.weathery.ui.theme.WeatheryTheme
import com.example.weathery.utils.LanguageUtils
import com.example.weathery.utils.LocationHandler
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var locationHandler: LocationHandler

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "device") ?: "device"
        val languageCode = if (lang == "device") java.util.Locale.getDefault().language else lang
        val wrapped = LanguageUtils.wrap(newBase, languageCode)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationHandler = LocationHandler(this)

        if (!locationHandler.checkPermissions()) {
            locationHandler.requestPermissions()
        }

        setContent {
            WeatheryTheme {
                var isLoading by remember { mutableStateOf(true) }
                var showOnboarding by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(1000)
                    val prefs = getSharedPreferences("weathery_prefs", Context.MODE_PRIVATE)
                    showOnboarding = !prefs.getBoolean("onboarding_completed", false)
                    isLoading = false
                }

                splashScreen.setKeepOnScreenCondition { isLoading }

                if (isLoading) return@WeatheryTheme

                if (showOnboarding) {
                    OnboardingScreen(
                        onDone = {
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
