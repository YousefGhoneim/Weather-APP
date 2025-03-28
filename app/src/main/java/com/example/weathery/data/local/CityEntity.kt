package com.example.weathery.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class CityEntity(
    @PrimaryKey val name: String,
    val lat: Double,
    val lon: Double
)
