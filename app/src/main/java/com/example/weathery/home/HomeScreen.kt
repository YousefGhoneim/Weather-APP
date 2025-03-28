package com.example.weathery.home

import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.weathery.R
import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.local.WeatherDatabase
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.models.*
import com.example.weathery.data.remote.RetrofitHelper
import com.example.weathery.data.remote.WeatherRemoteDataSource
import com.example.weathery.data.repo.WeatherRepository
import com.example.weathery.favourite.FavoritesViewModel
import com.example.weathery.utils.LocationHandler
import com.example.weathery.utils.getWeatherBorderColor

@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    favViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.FavoritesViewModelFactory(
        WeatherRepository(
            WeatherRemoteDataSource(RetrofitHelper.service),
            WeatherLocalDataSource(WeatherDatabase.getInstance(LocalContext.current).weatherDao())
        )
    ))
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val handler = remember { LocationHandler(activity) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var location by remember { mutableStateOf<Location?>(null) }
    var cityName by remember { mutableStateOf("Unknown") }
    var lastFetchedLocation by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(Unit) {
        if (!handler.checkPermissions()) {
            handler.requestPermissions()
            return@LaunchedEffect
        }

        if (!handler.isLocationEnabled()) {
            Toast.makeText(context, "Please enable location services", Toast.LENGTH_LONG).show()
            handler.openLocationSettings()
            return@LaunchedEffect
        }

        handler.getFreshLocation(
            onLocationFound = { loc, city ->
                location = loc
                cityName = city
            },
            onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    LaunchedEffect(location) {
        location?.let { newLocation ->
            val isNew = lastFetchedLocation == null ||
                    newLocation.distanceTo(lastFetchedLocation!!) > 100

            if (isNew) {
                viewModel.fetchWeather(newLocation.latitude, newLocation.longitude, cityName)
                lastFetchedLocation = newLocation
            } else {
                Log.d("HomeScreen", "Location unchanged. Skipping fetch.")
            }
        }
    }

    when (state) {
        is UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

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
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CurrentWeatherSection(data.current) {
                        location?.let {
                            val cityEntity = CityEntity(
                                name = data.current.cityName,
                                lat = it.latitude,
                                lon = it.longitude
                            )
                            favViewModel.saveCity(cityEntity)
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
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


@Composable
fun CurrentWeatherSection(
    weather: CurrentWeatherUiModel,
    onFavClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onFavClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Add to favorites",
                    tint = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = weather.description,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "${weather.temp}°",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🌇 Sunset ${weather.sunset}")
            Spacer(Modifier.width(16.dp))
            Text("🌅 Sunrise ${weather.sunrise}")
        }
    }
}


@Composable
fun ExtraMetricsSection(metrics: ExtraMetricsUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricItem("Pressure", "${metrics.pressure} hPa")
        MetricItem("Wind", "${metrics.windSpeed} ${metrics.windUnit}")
        MetricItem("Humidity", "${metrics.humidity}%")
        MetricItem("UV", "${metrics.uvi}")
        MetricItem("Clouds", "${metrics.clouds}%")
    }
}

@Composable
fun MetricItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelSmall)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun HourlyForecastSection(hourlyList: List<HourlyWeatherUiModel>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(hourlyList.size) { index ->
            val item = hourlyList[index]
            val borderColor = getWeatherBorderColor(item.description)

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .size(width = 80.dp, height = 130.dp)
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = MaterialTheme.shapes.medium
                    ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = item.time, style = MaterialTheme.typography.labelMedium)
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(text = "${item.temp}°", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}



@Composable
fun DailyForecastSection(dailyList: List<DailyWeatherUiModel>) {
    Column {
        Text("Next 7 Days", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        dailyList.forEach { day ->
            val borderColor = getWeatherBorderColor(day.description)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .border(
                        width = 2.dp,
                        color = borderColor,
                        shape = MaterialTheme.shapes.medium
                    ),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            )
        {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = day.dayName, style = MaterialTheme.typography.bodyMedium)
                        Text(text = day.date, style = MaterialTheme.typography.labelSmall)
                    }

                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = day.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = "Max: ${day.maxTemp}°", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Min: ${day.minTemp}°", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherLottieBackground(condition: String) {
    Log.i("TAG", "WeatherLottieBackground: $condition")
    val animationRes = when {
        "clear" in condition.lowercase() -> R.raw.sun
        "cloud" in condition.lowercase() -> R.raw.cloud
        "rain" in condition.lowercase() -> R.raw.rain
        "snow" in condition.lowercase() -> R.raw.snow
        else -> R.raw.cloud
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}


