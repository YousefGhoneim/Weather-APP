package com.example.weathery.home

import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.example.weathery.setting.SettingsViewModel
import com.example.weathery.utils.LocationHandler
import com.example.weathery.utils.getWeatherBorderColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    favViewModel: FavoritesViewModel,
    isConnected: Boolean,
    navigateToMap: () -> Unit
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val unitSystem by settingsViewModel.unitSystem.collectAsState()
    val tempUnit by settingsViewModel.tempUnit.collectAsState()
    val locationSource by settingsViewModel.locationSource.collectAsState()
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    var location by remember { mutableStateOf<Location?>(null) }
    var cityName by remember { mutableStateOf("Unknown") }
    var lastFetchedLocation by remember { mutableStateOf<Location?>(null) }
    var triggerMapNavigation by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(locationSource, permissionState.status) {
        if (locationSource == "gps") {
            if (!permissionState.status.isGranted) {
                if (permissionState.status.shouldShowRationale) {
                    Toast.makeText(context,
                        context.getString(R.string.permission_is_required_to_access_location), Toast.LENGTH_SHORT).show()
                    settingsViewModel.setLocationSource("map")
                    triggerMapNavigation = true
                } else {
                    permissionState.launchPermissionRequest()
                }
                return@LaunchedEffect
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    location = loc
                    val geocoder = android.location.Geocoder(context)
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val city = addresses?.firstOrNull()?.locality ?: "Unknown"
                    val admin = addresses?.firstOrNull()?.adminArea ?: ""
                    val country = addresses?.firstOrNull()?.countryName ?: ""
                    cityName = listOfNotNull(city, admin, country).filter { it.isNotBlank() }.joinToString(", ")
                } else {
                    Toast.makeText(context,
                        context.getString(R.string.unable_to_get_location), Toast.LENGTH_SHORT).show()
                    settingsViewModel.setLocationSource("map")
                    triggerMapNavigation = true
                }
            }
        } else if (locationSource == "map") {
            val picked = settingsViewModel.getPickedLocation()
            if (picked != null) {
                val (lat, lon, city) = picked
                homeViewModel.fetchWeather(lat, lon, city)
            } else {
                Toast.makeText(context,
                    context.getString(R.string.please_pick_a_location_from_map), Toast.LENGTH_SHORT).show()
                triggerMapNavigation = true
            }
        }
    }

    if (triggerMapNavigation) {
        triggerMapNavigation = false
        navigateToMap()
    }

    LaunchedEffect(location) {
        location?.let { newLocation ->
            val isNew = lastFetchedLocation == null || newLocation.distanceTo(lastFetchedLocation!!) > 100
            if (isNew) {
                homeViewModel.fetchWeather(newLocation.latitude, newLocation.longitude, cityName)
                lastFetchedLocation = newLocation
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success && !isConnected) {
            Toast.makeText(context,
                context.getString(R.string.showing_cached_weather_data), Toast.LENGTH_SHORT).show()
        }
    }

    when (uiState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        is UiState.Success -> {
            val data = (uiState as UiState.Success).data
            Box(Modifier.fillMaxSize()) {
                WeatherLottieBackground(data.current.main)
                Box(
                    Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
                )
                Column(
                    Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CurrentWeatherSection(data.current, tempUnit) {
                        location?.let {
                            val cityEntity = CityEntity(
                                name = data.current.cityName,
                                lat = it.latitude,
                                lon = it.longitude
                            )
                            favViewModel.saveCity(cityEntity)
                            Toast.makeText(context,
                                context.getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show()
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    ExtraMetricsSection(data.extra)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.hourly_details), style = MaterialTheme.typography.titleMedium)
                    HourlyForecastSection(data.hourly, tempUnit, data.extra.windUnit)
                    Spacer(Modifier.height(16.dp))
                    DailyForecastSection(data.daily, tempUnit, data.extra.windUnit)
                    Spacer(Modifier.height(16.dp))
                    BottomMetricsSection(data.extra)
                }
            }
        }

        is UiState.Error -> {
            val error = (uiState as UiState.Error).message
            Text(text = stringResource(R.string.error, error), style = MaterialTheme.typography.bodyMedium, color = Color.Red)
        }

        is UiState.Empty -> {
            Text(text = stringResource(R.string.no_data_available), style = MaterialTheme.typography.bodyMedium)
        }
    }
}



@Composable
fun CurrentWeatherSection(
    weather: CurrentWeatherUiModel,
    tempUnitLabel: String,
    onFavClick: () -> Unit
) {
    val tempSymbol = when (tempUnitLabel) {
        "metric" -> "°C"
        "imperial" -> "°F"
        else -> "K"
    }

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
            text = "${weather.temp}$tempUnitLabel",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.sunset, weather.sunset), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(16.dp))
            Text(text = stringResource(R.string.sunrise, weather.sunrise), style = MaterialTheme.typography.bodyMedium)

        }
    }
}



@Composable
fun HourlyForecastSection(hourlyList: List<HourlyWeatherUiModel>, tempUnitLabel: String, windUnit: String) {
    val tempSymbol = when (tempUnitLabel) {
        "metric" -> "°C"
        "imperial" -> "°F"
        else -> "K"
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(hourlyList.size) { index ->
            val item = hourlyList[index]
            val borderColor = getWeatherBorderColor(item.description)

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .size(width = 100.dp, height = 150.dp)
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
                    Text(text = "🌡️ ${item.temp}$tempUnitLabel", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "🌀 ${item.windSpeed} $windUnit", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun DailyForecastSection(dailyList: List<DailyWeatherUiModel>, tempUnitLabel: String, windUnit: String) {
    val tempSymbol = when (tempUnitLabel) {
        "metric" -> "°C"
        "imperial" -> "°F"
        else -> "K"
    }
    Column {
        Text(stringResource(R.string.next_7_days), style = MaterialTheme.typography.titleMedium)
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
            ) {
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
                        Text(stringResource(R.string.max, day.maxTemp, tempUnitLabel), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.min, day.minTemp, tempUnitLabel), style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.feels_like, day.feelsLike, tempUnitLabel), style = MaterialTheme.typography.labelSmall)
                        Text("🌀 ${day.windSpeed} $windUnit", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
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
        MetricItem(stringResource(R.string.pressure), "${metrics.pressure} hPa")
        MetricItem(stringResource(R.string.wind), "${metrics.windSpeed} ${metrics.windUnit}")
        MetricItem(stringResource(R.string.humidity), "${metrics.humidity}%")
        MetricItem(stringResource(R.string.uv), "${metrics.uvi}")
        MetricItem(stringResource(R.string.clouds), "${metrics.clouds}%")
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
fun WeatherLottieBackground(condition: String) {
    Log.i("TAG", "WeatherLottieBackground: $condition")
    val animationRes = when {
        "clear" in condition.lowercase() -> R.raw.sunny_modified
        "cloud" in condition.lowercase() -> R.raw.cloud
        "rain" in condition.lowercase() -> R.raw.rainy
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

@Composable
fun MetricArc(title: String, value: Float, max: Float, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        Canvas(modifier = Modifier.size(72.dp)) {
            drawArc(
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                color = Color(0xFFF1F1F1),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                startAngle = 150f,
                sweepAngle = (value / max) * 240f,
                useCenter = false,
                color = color,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text("${value.toInt()} $unit", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun BottomMetricsSection(metrics: ExtraMetricsUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.environment_metrics), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                MetricArcCard(stringResource(R.string.pressuree), metrics.pressure.toFloat(), 1100f, "hPa", Color(0xFFfc466b))
            }
            item {
                MetricArcCard(stringResource(R.string.humidityy), metrics.humidity.toFloat(), 100f, "%", Color(0xFF1c92d2))
            }
            item {
                MetricArcCard(stringResource(R.string.windd), metrics.windSpeed.toFloat(), 30f, metrics.windUnit, Color(0xFF36d1dc))
            }
            item {
                MetricArcCard(stringResource(R.string.uvv), metrics.uvi.toFloat(), 11f, "", Color(0xFFf7971e))
            }
        }

    }
}

@Composable
fun MetricArcCard(
    title: String,
    value: Float,
    max: Float,
    unit: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(100.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(2.dp, color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Canvas(modifier = Modifier.size(52.dp)) {
                drawArc(
                    startAngle = 150f,
                    sweepAngle = 240f,
                    useCenter = false,
                    color = Color(0xFFF1F1F1),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    startAngle = 150f,
                    sweepAngle = (value / max) * 240f,
                    useCenter = false,
                    color = color,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text("${value.toInt()} $unit", style = MaterialTheme.typography.bodySmall)
        }
    }
}



