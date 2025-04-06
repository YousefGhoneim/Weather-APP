package com.example.weathery

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import com.example.weathery.data.models.ExtraMetricsUiModel
import com.example.weathery.home.BottomMetricsSection
import com.example.weathery.home.HomeViewModel
import com.example.weathery.setting.SettingsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewWeatherScreen(
    lat: Double,
    lon: Double,
    city: String,
    weatherViewModel: HomeViewModel,
    favViewModel: FavoritesViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    var resolvedCityName by remember { mutableStateOf(city) }
    val favoritesState by favViewModel.favoritesState.collectAsStateWithLifecycle()

    val unitSystem by settingsViewModel.unitSystem.collectAsState()
    val tempUnit by settingsViewModel.tempUnit.collectAsState()

    LaunchedEffect(Unit) {
        val geocoder = Geocoder(context)
        val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
        resolvedCityName = listOfNotNull(
            address?.locality,
            address?.adminArea,
            address?.countryName
        ).filter { it.isNotBlank() }.joinToString(", ")

        weatherViewModel.fetchWeather(lat, lon, resolvedCityName)
    }

    LaunchedEffect(favoritesState) {
        if (favoritesState is UiState.Success) {
            val list = (favoritesState as UiState.Success<List<CityEntity>>).data
            isFavorite = list.any { it.lat == lat && it.lon == lon }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather_in, resolvedCityName)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isFavorite) {
                        IconButton(onClick = {
                            val cityEntity = CityEntity(name = resolvedCityName, lat = lat, lon = lon)
                            favViewModel.saveCity(cityEntity)
                            Toast.makeText(context,
                                context.getString(R.string.added_to_favoritess), Toast.LENGTH_SHORT).show()
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
                    WeatherLottieBackground(data.current.main)

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
                    )

                    Column(
                        modifier = Modifier
                            .padding(top = padding.calculateTopPadding())
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CurrentWeatherSection(data.current, tempUnit, onFavClick = {})
                        Spacer(Modifier.height(16.dp))
                        ExtraMetricsSection(data.extra)
                        Spacer(Modifier.height(16.dp))
                        Text("Hourly Details", style = MaterialTheme.typography.titleMedium)
                        HourlyForecastSection(data.hourly, tempUnit, data.extra.windUnit)
                        Spacer(Modifier.height(16.dp))
                        DailyForecastSection(data.daily, tempUnit, data.extra.windUnit)
                        BottomMetricsSection(data.extra)
                    }
                }
            }

            is UiState.Error -> Text(
                stringResource(
                    R.string.errorr,
                    (state as UiState.Error).message
                ))
            is UiState.Empty -> Text(stringResource(R.string.no_data_availablee))
        }
    }
}
