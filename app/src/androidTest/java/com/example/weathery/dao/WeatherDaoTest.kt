package com.example.weathery.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weathery.data.local.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class WeatherDaoTest {

    private lateinit var db: WeatherDatabase
    private lateinit var dao: WeatherDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.weatherDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertCity_and_getAllCities_shouldReturnInsertedCity() = runTest {
        val city = CityEntity(name = "Jeddah", lat = 21.5, lon = 39.2)
        dao.insertCity(city)

        val result = dao.getAllCities().first()

        assertEquals(1, result.size)
        assertEquals(city, result.first())
    }

    @Test
    fun deleteCity_shouldRemoveCity() = runTest {
        val city = CityEntity(name = "Riyadh", lat = 24.7, lon = 46.7)
        dao.insertCity(city)

        dao.deleteCity(city)
        val result = dao.getAllCities().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun insertAlarm_and_getAlarmById_shouldReturnAlarm() = runTest {
        val alarm = WeatherAlarmEntity(cityName = "Dammam", lat = 26.4, lon = 50.1, startTime = 1000L, endTime = 2000L)
        val id = dao.insertAlarm(alarm).toInt()

        val result = dao.getAlarmById(id)

        assertNotNull(result)
        assertEquals("Dammam", result?.cityName)
    }

    @Test
    fun deleteAlarm_shouldRemoveAlarm() = runTest {
        val alarm = WeatherAlarmEntity(cityName = "Mecca", lat = 21.4, lon = 39.8, startTime = 1000L, endTime = 2000L)
        dao.insertAlarm(alarm)

        val alarmsBefore = dao.getAllAlarms().first()
        dao.deleteAlarm(alarmsBefore.first())
        val alarmsAfter = dao.getAllAlarms().first()

        assertTrue(alarmsAfter.isEmpty())
    }

    @Test
    fun insertCachedWeather_and_getCachedWeather_shouldReturnIt() = runTest {
        val entity = CachedWeatherEntity(cityName = "Abha", dataJson = "{\"temp\":25}")
        dao.insertCachedWeather(entity)

        val result = dao.getCachedWeather()

        assertNotNull(result)
        assertEquals("Abha", result?.cityName)
    }
}
