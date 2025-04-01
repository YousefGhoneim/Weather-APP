package com.example.weathery

import OsmMapPickerScreen
import android.location.Geocoder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.weathery.alarm.AlarmScreen
import com.example.weathery.alarm.AddAlarmScreen
import com.example.weathery.alarm.AlarmsViewModel
import com.example.weathery.data.local.CityEntity
import com.example.weathery.data.local.WeatherDatabase
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.remote.RetrofitHelper
import com.example.weathery.data.remote.WeatherRemoteDataSource
import com.example.weathery.data.repo.WeatherRepository
import com.example.weathery.favourite.FavoritesScreen
import com.example.weathery.favourite.FavoritesViewModel
import com.example.weathery.home.HomeScreen
import com.example.weathery.home.WeatherViewModel
import com.example.weathery.setting.SettingsScreen
import com.example.weathery.setting.SettingsViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsViewModel = remember { SettingsViewModel(context) }

    val items = listOf(
        ScreenRoute.Home,
        ScreenRoute.Favorites,
        ScreenRoute.Alarm,
        ScreenRoute.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = currentRoute(navController)
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.label)
                        },
                        label = {
                            Text(screen.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenRoute.Home.route) {
                val viewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.WeatherViewModelFactory(
                    WeatherRepository(
                        WeatherRemoteDataSource(RetrofitHelper.service),
                        WeatherLocalDataSource(WeatherDatabase.getInstance(context).weatherDao())
                    )
                ))
                HomeScreen(
                    viewModel = viewModel,
                    navigateToMap = {
                        navController.navigate("mapPickerFromSettings")
                    }
                )
            }

            composable(ScreenRoute.Favorites.route) {
                val favViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.FavoritesViewModelFactory(
                    WeatherRepository(
                        WeatherRemoteDataSource(RetrofitHelper.service),
                        WeatherLocalDataSource(WeatherDatabase.getInstance(context).weatherDao())
                    )
                ))
                FavoritesScreen(
                    viewModel = favViewModel,
                    onPickLocation = {
                        navController.navigate("mapPickerFromFavorites")
                    },
                    onCityClick = { lat, lon, city ->
                        navController.navigate("preview/$lat/$lon/$city")
                    }
                )
            }

            composable(ScreenRoute.Alarm.route) {
                val alarmsViewModel: AlarmsViewModel = viewModel(factory = AlarmsViewModel.AlarmsViewModelFactory(
                    WeatherRepository(
                        WeatherRemoteDataSource(RetrofitHelper.service),
                        WeatherLocalDataSource(WeatherDatabase.getInstance(context).weatherDao())
                    )
                ))

                AlarmScreen(
                    viewModel = alarmsViewModel,
                    navController = navController,
                    onPickLocationFromMap = { onLocationSelected ->
                        navController.navigate("mapPickerFromAlarm")
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.getLiveData<Triple<Double, Double, String>>("picked_alarm_location")
                            ?.observeForever { location ->
                                location?.let {
                                    onLocationSelected(it.first, it.second, it.third)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.remove<Triple<Double, Double, String>>("picked_alarm_location")
                                }
                            }
                    }
                )
            }

            composable("addAlarm") { backStackEntry ->
                val alarmsViewModel: AlarmsViewModel = viewModel(factory = AlarmsViewModel.AlarmsViewModelFactory(
                    WeatherRepository(
                        WeatherRemoteDataSource(RetrofitHelper.service),
                        WeatherLocalDataSource(WeatherDatabase.getInstance(context).weatherDao())
                    )
                ))

                val savedStateHandle = backStackEntry.savedStateHandle
                val location by savedStateHandle.getStateFlow<Triple<Double, Double, String>?>(
                    "picked_add_alarm_location", null
                ).collectAsState()

                AddAlarmScreen(
                    selectedLocation = location,
                    onBack = {
                        navController.navigate(ScreenRoute.Alarm.route) {
                            popUpTo("addAlarm") { inclusive = true }
                        }
                    },
                    onPickLocationFromMap = {
                        navController.navigate("mapPickerForAddAlarm")
                    },
                    onSave = { alarm ->
                        alarmsViewModel.addAlarm(alarm)
                        navController.navigate(ScreenRoute.Alarm.route) {
                            popUpTo("addAlarm") { inclusive = true }
                        }
                    },
                    viewModel = alarmsViewModel
                )

                LaunchedEffect(location) {
                    if (location != null) {
                        savedStateHandle["picked_add_alarm_location"] = null
                    }
                }
            }

            composable("mapPickerForAddAlarm") {
                OsmMapPickerScreen(
                    onLocationSelected = { lat, lon, city ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("picked_add_alarm_location", Triple(lat, lon, city))
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("preview/{lat}/{lon}/{city}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lon") { type = NavType.StringType },
                    navArgument("city") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                val city = backStackEntry.arguments?.getString("city") ?: "Unknown"

                val weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.WeatherViewModelFactory(
                    WeatherRepository(
                        WeatherRemoteDataSource(RetrofitHelper.service),
                        WeatherLocalDataSource(WeatherDatabase.getInstance(context).weatherDao())
                    )
                ))

                val favViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.FavoritesViewModelFactory(
                    WeatherRepository(
                        WeatherRemoteDataSource(RetrofitHelper.service),
                        WeatherLocalDataSource(WeatherDatabase.getInstance(context).weatherDao())
                    )
                ))

                PreviewWeatherScreen(
                    lat = lat,
                    lon = lon,
                    city = city,
                    viewModel = weatherViewModel,
                    favViewModel = favViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("mapPickerFromSettings") {
                OsmMapPickerScreen(
                    onLocationSelected = { lat, lon, city ->
                        settingsViewModel.setPickedLocation(lat, lon, city)
                        navController.popBackStack(ScreenRoute.Home.route, inclusive = false)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("mapPickerFromFavorites") {
                OsmMapPickerScreen(
                    onLocationSelected = { lat, lon, city ->
                        navController.navigate("preview/$lat/$lon/$city")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("mapPickerFromAlarm") {
                OsmMapPickerScreen(
                    onLocationSelected = { lat, lon, city ->
                        navController.navigate("preview/$lat/$lon/$city")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoute.Settings.route) {
                SettingsScreen(
                    onRequestMapPicker = {
                        navController.navigate("mapPickerFromSettings")
                    }
                )
            }
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}