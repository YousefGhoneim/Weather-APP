package com.example.weathery.alarm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weathery.PreviewWeatherScreen
import com.example.weathery.favourite.FavoritesViewModel
import com.example.weathery.home.HomeViewModel
import com.example.weathery.setting.SettingsViewModel
import com.example.weathery.ui.theme.WeatheryTheme
import com.example.weathery.utils.RepositoryProvider

class PreviewWeatherActivity : AppCompatActivity() {
    private lateinit var weatherViewModel: HomeViewModel
    private lateinit var favViewModel: FavoritesViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weatherRepository = RepositoryProvider.provideRepository(this)

        weatherViewModel = ViewModelProvider(
            this,
            HomeViewModel.Factory(weatherRepository)
        )[HomeViewModel::class.java]

        favViewModel = ViewModelProvider(
            this,
            FavoritesViewModel.FavoritesViewModelFactory(weatherRepository)
        )[FavoritesViewModel::class.java]

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.Factory(weatherRepository)
        )[SettingsViewModel::class.java]

        val city = intent.getStringExtra("city") ?: "Unknown City"
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)

        setContent {
            WeatheryTheme {
                PreviewWeatherScreen(
                    lat = lat,
                    lon = lon,
                    city = city,
                    weatherViewModel = weatherViewModel,
                    favViewModel = favViewModel,
                    settingsViewModel = settingsViewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
