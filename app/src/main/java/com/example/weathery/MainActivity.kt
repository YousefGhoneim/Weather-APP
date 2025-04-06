package com.example.weathery

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.weathery.ui.theme.WeatheryTheme
import com.example.weathery.utils.LanguageUtils
import com.example.weathery.utils.LocationHandler
import kotlinx.coroutines.delay
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var locationHandler: LocationHandler

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "device") ?: "device"
        val languageCode = if (lang == "device") Locale.getDefault().language else lang
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
                    Log.d("DEBUG_ONBOARD", "Show onboarding: $showOnboarding")
                    isLoading = false
                }

                splashScreen.setKeepOnScreenCondition { isLoading }

                when {
//                    isLoading -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator()
//                        }
//                    }
                    showOnboarding -> {
                        OnboardingScreen(
                            onDone = {
                                getSharedPreferences("weathery_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("onboarding_completed", true)
                                    .apply()
                                showOnboarding = false
                            }
                        )
                    }
                    else -> {
                        MainNavigation()
                    }
                }
            }
        }
    }
}
