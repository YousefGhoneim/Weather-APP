package com.example.weathery.alarm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weathery.PreviewWeatherScreen
import com.example.weathery.data.local.WeatherDatabase
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.remote.RetrofitHelper
import com.example.weathery.data.remote.WeatherRemoteDataSource
import com.example.weathery.data.repo.WeatherRepository
import com.example.weathery.favourite.FavoritesViewModel
import com.example.weathery.home.WeatherViewModel

class PreviewWeatherActivity : AppCompatActivity() {
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var favViewModel: FavoritesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val weatherRepository = WeatherRepository.getInstance(
            WeatherRemoteDataSource(RetrofitHelper.service),
            WeatherLocalDataSource(WeatherDatabase.getInstance(this).weatherDao())
        )
        weatherViewModel = ViewModelProvider(
            this,
            WeatherViewModel.WeatherViewModelFactory(weatherRepository)
        )[WeatherViewModel::class.java]

        favViewModel = ViewModelProvider(
            this,
            FavoritesViewModel.FavoritesViewModelFactory(weatherRepository)
        )[FavoritesViewModel::class.java]

        // Extract data passed from the notification click
        val city = intent.getStringExtra("city") ?: "Unknown City"
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        val alarmId = intent.getIntExtra("alarm_id", -1)

        setContent {
            PreviewWeatherScreen( lat, lon, city, weatherViewModel, favViewModel, onBack = { finish() } )
        }
    }
}
