package com.example.weathery.favourite

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import com.example.weathery.R
import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.models.UiState
import com.example.weathery.utils.getWeatherBorderColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.*

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onPickLocation: () -> Unit,
    onCityClick: (lat: Double, lon: Double, city: String) -> Unit
) {
    val state by viewModel.favoritesState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deletedCityForUndo by remember { mutableStateOf<CityEntity?>(null) }

    LaunchedEffect(deletedCityForUndo) {
        deletedCityForUndo?.let { city ->
            val result = snackbarHostState.showSnackbar(
                message = "${city.name} removed",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.saveCity(city)
            }
            deletedCityForUndo = null
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    modifier = Modifier.offset(y = 2.dp),
                    onClick = onPickLocation,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Pick location to add")
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val favorites = if (state is UiState.Success<*>) {
                (state as UiState.Success<List<CityEntity>>).data
            } else emptyList()

            val bgCondition = if (favorites.isEmpty()) "clear" else "cloud"
            WeatherLottieBackground(condition = bgCondition)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        if (favorites.isEmpty())
                            Color(0xFF0D1B2A).copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                when (state) {
                    is UiState.Loading -> CircularProgressIndicator()
                    is UiState.Error -> Text("Error loading favorites")
                    is UiState.Empty -> Text("No favorite cities yet")
                    is UiState.Success<*> -> {
                        LazyColumn {
                            items(favorites.size, key = { favorites[it].name }) { index ->
                                val city = favorites[index]
                                val dismissState = rememberSwipeToDismissBoxState(
                                    initialValue = SwipeToDismissBoxValue.Settled
                                )
                                val currentCity by rememberUpdatedState(city)

                                LaunchedEffect(dismissState.currentValue) {
                                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                        viewModel.deleteCity(currentCity)
                                        deletedCityForUndo = currentCity
                                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                    }
                                }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {},
                                    content = {
                                        FancyCityCard(
                                            city = city,
                                            onClick = {
                                                onCityClick(city.lat, city.lon, city.name)
                                            },
                                            onDelete = {
                                                deletedCityForUndo = city
                                                viewModel.deleteCity(city)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FancyCityCard(
    city: CityEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = getWeatherBorderColor(city.name)
    val scope = rememberCoroutineScope()
    var animateDelete by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animateDelete) 1.3f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "DeleteIconScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (animateDelete) 15f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "DeleteIconRotate"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(width = 2.dp, color = borderColor, shape = MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable { onClick() }
            )
            IconButton(
                onClick = {
                    animateDelete = true
                    onDelete()
                    scope.launch {
                        delay(300)
                        animateDelete = false
                    }
                },
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove city",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun WeatherLottieBackground(condition: String) {
    val animationRes = when {
        "clear" in condition.lowercase() -> R.raw.fav_background
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
