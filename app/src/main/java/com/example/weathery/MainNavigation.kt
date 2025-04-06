package com.example.weathery

import OsmMapPickerScreen
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.weathery.alarm.AddAlarmScreen
import com.example.weathery.alarm.AlarmScreen
import com.example.weathery.alarm.AlarmsViewModel
import com.example.weathery.favourite.FavoritesScreen
import com.example.weathery.favourite.FavoritesViewModel
import com.example.weathery.home.HomeScreen
import com.example.weathery.home.HomeViewModel
import com.example.weathery.setting.SettingsScreen
import com.example.weathery.setting.SettingsViewModel
import com.example.weathery.utils.ConnectionLiveData
import com.example.weathery.utils.RepositoryProvider

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.provideRepository(context) }

    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository))

    val items = listOf(
        ScreenRoute.Home,
        ScreenRoute.Favorites,
        ScreenRoute.Alarm,
        ScreenRoute.Settings
    )

    // Network connectivity state
    val snackbarHostState = remember { SnackbarHostState() }
    val connectionLiveData = remember { ConnectionLiveData(context) }
    val isConnected by connectionLiveData.observeAsState(initial = true)

    LaunchedEffect(isConnected) {
        if (isConnected == false) {
            val result = snackbarHostState.showSnackbar(
                message = "No internet connection",
                actionLabel = "Settings",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val currentRoute = currentRoute(navController)
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
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
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
                val favViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.FavoritesViewModelFactory(repository))
                HomeScreen(
                    homeViewModel = homeViewModel,
                    favViewModel = favViewModel,
                    settingsViewModel = settingsViewModel,
                    isConnected = isConnected,
                    navigateToMap = { navController.navigate("mapPickerFromSettings") }
                )
            }


            composable(ScreenRoute.Favorites.route) {
                val favViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.FavoritesViewModelFactory(repository))
                FavoritesScreen(
                    viewModel = favViewModel,
                    onPickLocation = { navController.navigate("mapPickerFromFavorites") },
                    onCityClick = { lat, lon, city ->
                        navController.navigate("preview/$lat/$lon/$city")
                    }
                )
            }

            composable(ScreenRoute.Alarm.route) { backStackEntry ->
                val alarmsViewModel: AlarmsViewModel = viewModel(factory = AlarmsViewModel.Factory(repository))
                val savedStateHandle = backStackEntry.savedStateHandle

                val pickedLocation by savedStateHandle
                    .getStateFlow<Triple<Double, Double, String>?>("picked_alarm_location", null)
                    .collectAsState()

                AlarmScreen(
                    viewModel = alarmsViewModel,
                    navController = navController,
                    onPickLocationFromMap = {
                        navController.navigate("mapPickerFromAlarm")
                    }
                )

                LaunchedEffect(pickedLocation) {
                    pickedLocation?.let {
                        alarmsViewModel.setPickedAlarmLocation(it.first, it.second, it.third)
                        savedStateHandle["picked_alarm_location"] = null
                    }
                }
            }

            composable("addAlarm") { backStackEntry ->
                val alarmsViewModel: AlarmsViewModel = viewModel(factory = AlarmsViewModel.Factory(repository))
                val savedStateHandle = backStackEntry.savedStateHandle
                val location by savedStateHandle
                    .getStateFlow<Triple<Double, Double, String>?>("picked_add_alarm_location", null)
                    .collectAsState()

                AddAlarmScreen(
                    selectedLocation = location,
                    viewModel = alarmsViewModel,
                    onBack = {
                        navController.navigate(ScreenRoute.Alarm.route) {
                            popUpTo("addAlarm") { inclusive = true }
                        }
                    },
                    onPickLocationFromMap = {
                        navController.navigate("mapPickerForAddAlarm")
                    }
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

            composable("mapPickerFromAlarm") {
                OsmMapPickerScreen(
                    onLocationSelected = { lat, lon, city ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("picked_alarm_location", Triple(lat, lon, city))
                        navController.popBackStack()
                    },
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

            composable(
                "preview/{lat}/{lon}/{city}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lon") { type = NavType.StringType },
                    navArgument("city") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                val city = backStackEntry.arguments?.getString("city") ?: "Unknown"
                val weatherViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
                val favViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.FavoritesViewModelFactory(repository))
                PreviewWeatherScreen(
                    lat = lat,
                    lon = lon,
                    city = city,
                    weatherViewModel = weatherViewModel,
                    favViewModel = favViewModel,
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoute.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onRequestMapPicker = { navController.navigate("mapPickerFromSettings") }
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
