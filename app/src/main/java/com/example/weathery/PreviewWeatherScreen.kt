package com.example.weathery

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.favourite.FavoritesViewModel
import com.example.weathery.home.CurrentWeatherSection
import com.example.weathery.home.DailyForecastSection
import com.example.weathery.home.ExtraMetricsSection
import com.example.weathery.home.HourlyForecastSection
import com.example.weathery.home.WeatherLottieBackground
import com.example.weathery.home.WeatherViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewWeatherScreen(
    lat: Double,
    lon: Double,
    city: String,
    viewModel: WeatherViewModel,
    favViewModel: FavoritesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    val favoritesState by favViewModel.favoritesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(lat, lon, city)
    }

    LaunchedEffect(favoritesState) {
        if (favoritesState is UiState.Success) {
            val list = (favoritesState as UiState.Success<List<CityEntity>>).data
            val matched = list.any { it.lat == lat && it.lon == lon }
            isFavorite = matched
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather in $city") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isFavorite) {
                        IconButton(onClick = {
                            val cityEntity = CityEntity(name = city, lat = lat, lon = lon)
                            favViewModel.saveCity(cityEntity)
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Add to favorites"
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Already in favorites",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

            )
        }
    ) { padding ->
        when (state) {
            is UiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is UiState.Success -> {
                val data = (state as UiState.Success).data

                Box(modifier = Modifier.fillMaxSize()) {
                    WeatherLottieBackground(data.current.description)

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
                    )

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CurrentWeatherSection(data.current, onFavClick = {})
                        Spacer(Modifier.height(16.dp))
                        ExtraMetricsSection(data.extra)
                        Spacer(Modifier.height(16.dp))
                        Text("Hourly Details", style = MaterialTheme.typography.titleMedium)
                        HourlyForecastSection(data.hourly)
                        Spacer(Modifier.height(16.dp))
                        DailyForecastSection(data.daily)
                    }
                }
            }

            is UiState.Error -> Text("Error: ${(state as UiState.Error).message}")
            is UiState.Empty -> Text("No data available.")
        }
    }
}
