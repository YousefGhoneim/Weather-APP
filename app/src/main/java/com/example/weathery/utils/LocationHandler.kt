package com.example.weathery.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import java.util.*

const val REQUEST_LOCATION_CODE = 2005

class LocationHandler(private val activity: Activity) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(activity)

    fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_CODE
        )
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivity(intent)
    }

    fun getAddressFromLocation(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(activity, Locale.getDefault())
            val address = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
            address?.locality ?: address?.adminArea ?: "Unknown location"
        } catch (e: Exception) {
            "Unknown location"
        }
    }

    @SuppressLint("MissingPermission")
    fun getFreshLocation(
        onLocationFound: (Location, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = LocationRequest.create().apply {
            interval = 10000L
            fastestInterval = 5000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (!checkPermissions()) {
            onError("Permissions not granted")
            return
        }

        try {
            fusedClient.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation ?: Location(LocationManager.GPS_PROVIDER)
                        val address = getAddressFromLocation(location.latitude, location.longitude)
                        onLocationFound(location, address)
                        fusedClient.removeLocationUpdates(this)
                    }
                },
                Looper.myLooper() ?: Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            onError("SecurityException: ${e.localizedMessage}")
        }
    }

}
