import android.Manifest
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsmMapPickerScreen(
    onLocationSelected: (lat: Double, lon: Double, city: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var selectedCity by remember { mutableStateOf("No location selected") }
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // Map View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val map = MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(5.0)
                        controller.setCenter(GeoPoint(30.0444, 31.2357))
                    }

                    if (ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        val locationOverlay = MyLocationNewOverlay(map).apply {
                            enableMyLocation()
                            runOnFirstFix {
                                val loc = myLocation
                                loc?.let {
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        map.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                                    }
                                }
                            }
                        }
                        map.overlays.add(locationOverlay)
                    }

                    // Manual tap to pick location
                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { point ->
                                selectedPoint = point

                                marker?.let { map.overlays.remove(it) }

                                marker = Marker(map).apply {
                                    position = point
                                    title = "Selected Location"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                map.overlays.add(marker)
                                map.invalidate()

                                val geocoder = Geocoder(ctx, Locale.getDefault())
                                selectedCity = try {
                                    geocoder.getFromLocation(
                                        point.latitude,
                                        point.longitude,
                                        1
                                    )?.firstOrNull()?.locality ?: "Selected"
                                } catch (e: Exception) {
                                    "Selected"
                                }
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?) = false
                    }

                    val mapEventsOverlay = MapEventsOverlay(receiver)
                    map.overlays.add(mapEventsOverlay)

                    mapView = map
                    map
                }
            )

            // Search Input and Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search city", color = Color.Black) }, // label color
                    textStyle = LocalTextStyle.current.copy(color = Color.Black), // input text color
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        performSearch(
                            context = context,
                            query = searchQuery,
                            onResult = { point, name ->
                                selectedPoint = point
                                selectedCity = name

                                mapView?.controller?.animateTo(point)
                                mapView?.controller?.setZoom(10.0)

                                marker?.let { mapView?.overlays?.remove(it) }
                                val newMarker = Marker(mapView).apply {
                                    position = point
                                    title = name
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mapView?.overlays?.add(newMarker)
                                mapView?.invalidate()
                                marker = newMarker
                            },
                            onNotFound = {
                                Toast.makeText(context, "City not found", Toast.LENGTH_SHORT).show()
                            }
                        )
                    })
                )
                IconButton(onClick = {
                    performSearch(
                        context = context,
                        query = searchQuery,
                        onResult = { point, name ->
                            selectedPoint = point
                            selectedCity = name

                            mapView?.controller?.animateTo(point)
                            mapView?.controller?.setZoom(10.0)

                            marker?.let { mapView?.overlays?.remove(it) }
                            val newMarker = Marker(mapView).apply {
                                position = point
                                title = name
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView?.overlays?.add(newMarker)
                            mapView?.invalidate()
                            marker = newMarker
                        },
                        onNotFound = {
                            Toast.makeText(context, "City not found", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search" , tint = Color.Black)

                }
            }


            // Confirm button
            Button(
                onClick = {
                    selectedPoint?.let { onLocationSelected(it.latitude, it.longitude, selectedCity) }
                },
                enabled = selectedPoint != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Confirm Location")
            }
            // City label at the bottom
            Text(
                text = "City: $selectedCity",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

        }
    }
}

private fun performSearch(
    context: android.content.Context,
    query: String,
    onResult: (GeoPoint, String) -> Unit,
    onNotFound: () -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val result = geocoder.getFromLocationName(query, 1)?.firstOrNull()
        if (result != null) {
            val point = GeoPoint(result.latitude, result.longitude)
            val name = result.locality ?: result.featureName ?: "Selected"
            onResult(point, name)
        } else {
            onNotFound()
        }
    } catch (e: Exception) {
        onNotFound()
    }
}
