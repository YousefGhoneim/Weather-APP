package com.example.weathery.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weathery.data.local.*
import com.example.weathery.data.models.CurrentWeatherUiModel
import com.example.weathery.data.models.DailyWeatherUiModel
import com.example.weathery.data.models.ExtraMetricsUiModel
import com.example.weathery.data.models.HourlyWeatherUiModel
import com.example.weathery.data.models.WeatherUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class WeatherLocalDataSourceTest {

    private lateinit var db: WeatherDatabase
    private lateinit var dao: WeatherDao
    private lateinit var prefs: SettingsPreferences
    private lateinit var localDataSource: WeatherLocalDataSource

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.weatherDao()
        prefs = SettingsPreferences(context)
        localDataSource = WeatherLocalDataSource(dao, prefs)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertCity_and_getAllCities_shouldReturnCity() = runTest {
        val city = CityEntity("Riyadh", 24.7, 46.7)
        localDataSource.insertCity(city)

        val result = localDataSource.getAllCities().first()
        assertEquals(1, result.size)
        assertEquals("Riyadh", result.first().name)
    }

    @Test
    fun insertAlarm_and_getAlarmById_shouldReturnAlarm() = runTest {
        val alarm = WeatherAlarmEntity(
            cityName = "Jeddah",
            lat = 21.5,
            lon = 39.2,
            startTime = 123456L,
            endTime = 654321L
        )
        val id = localDataSource.insertAlarm(alarm)
        val result = localDataSource.getAlarmById(id)

        assertNotNull(result)
        assertEquals("Jeddah", result?.cityName)
    }

    @Test
    fun saveCachedWeather_and_getCachedWeather_shouldReturnCorrectWeather() = runTest {
        val weather = WeatherUiModel(
            current = CurrentWeatherUiModel(
                temp = 30,
                feelsLike = 32,
                description = "Sunny",
                iconRes = 123,
                date = "2025-04-05",
                cityName = "Test City",
                sunrise = "06:00",
                sunset = "18:30",
                main = "Clear"
            ),
            hourly = listOf(
                HourlyWeatherUiModel(
                    time = "12:00",
                    iconRes = 123,
                    temp = 30,
                    feelsLike = 32,
                    description = "Sunny",
                    windSpeed = 5.5
                )
            ),
            daily = listOf(
                DailyWeatherUiModel(
                    dayName = "Saturday",
                    date = "2025-04-05",
                    maxTemp = 34,
                    minTemp = 22,
                    feelsLike = 28,
                    iconRes = 123,
                    description = "Hot",
                    windSpeed = 6.5
                )
            ),
            extra = ExtraMetricsUiModel(
                pressure = 1010,
                humidity = 40,
                windSpeed = 6.5,
                uvi = 8.0,
                clouds = 10,
                windUnit = "m/s"
            )
        )

        localDataSource.saveCachedWeather("Mecca", weather)
        val result = localDataSource.getCachedWeather()

        assertNotNull(result)
        assertEquals("Test City", result?.current?.cityName)
        assertEquals(30, result?.current?.temp)
        assertEquals("Sunny", result?.current?.description)
    }

    @Test
    fun deleteCity_shouldRemoveItFromList() = runTest {
        val city = CityEntity("Abha", 18.2, 42.5)
        localDataSource.insertCity(city)
        localDataSource.deleteCity(city)

        val result = localDataSource.getAllCities().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteExpiredAlarms_shouldRemovePastAlarmsOnly() = runTest {
        val past = System.currentTimeMillis() - 60_000
        val future = System.currentTimeMillis() + 60_000

        localDataSource.insertAlarm(WeatherAlarmEntity(cityName = "Old", lat = 0.0, lon = 0.0, startTime = past, endTime = past + 1000))
        localDataSource.insertAlarm(WeatherAlarmEntity(cityName = "New", lat = 0.0, lon = 0.0, startTime = future, endTime = future + 1000))

        localDataSource.deleteExpiredAlarms(System.currentTimeMillis())

        val alarms = localDataSource.getAllAlarms().first()
        assertEquals(1, alarms.size)
        assertEquals("New", alarms.first().cityName)
    }
}
