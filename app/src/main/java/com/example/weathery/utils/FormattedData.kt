package com.example.weathery.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




fun Long.toFormatted(pattern: String): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this * 1000))
}


fun getWeatherBorderColor(description: String): Color {
    return when {
        "clear" in description.lowercase() -> Color(0xFFFFF176)  // Sunny yellow
        "cloud" in description.lowercase() -> Color(0xFF90A4AE)  // Cloudy gray-blue
        "rain" in description.lowercase() -> Color(0xFF4FC3F7)   // Light blue for rain
        "snow" in description.lowercase() -> Color(0xFFB3E5FC)   // Pale blue for snow
        "storm" in description.lowercase() -> Color(0xFF9575CD)  // Purple for thunder
        else -> Color(0xFF81D4FA)                                 // Default: sky blue
    }
}
