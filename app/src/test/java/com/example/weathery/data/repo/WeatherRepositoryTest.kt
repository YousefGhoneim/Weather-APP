package com.example.weathery.data.repo

import com.example.weathery.data.local.FakeLocalDataSource
import com.example.weathery.data.models.CurrentWeatherUiModel
import com.example.weathery.data.models.ExtraMetricsUiModel
import com.example.weathery.data.models.WeatherUiModel
import com.example.weathery.data.remote.FakeRemoteDataSource
import junit.framework.TestCase.assertNotNull

import junit.framework.TestCase.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class WeatherRepositoryTest {

    private lateinit var local: FakeLocalDataSource
    private lateinit var remote: FakeRemoteDataSource
    private lateinit var repo: IWeatherRepository

    @Before
    fun setup() {
        local = FakeLocalDataSource()
        remote = FakeRemoteDataSource()
        repo = WeatherRepository(remote, local)
    }

    @Test
    fun fetchWeather_shouldCallRemote_andReturnOneResponse() = runTest {
        val result = repo.fetchWeather(24.7, 46.7, "metric")

        assertNotNull(result)
        assert(result.lat == 24.7)
        assert(result.lon == 46.7)
        assert(remote.lastRequestedUnit == "metric")
    }

    @Test
    fun fetchWeather_shouldThrow_whenRemoteFails() = runTest {
        remote.shouldFail = true

        try {
            repo.fetchWeather(0.0, 0.0, "metric")
            fail("Expected exception not thrown")
        } catch (e: Exception) {
            assert(true)
        }
    }

    @Test
    fun cacheWeather_and_getCachedWeather_shouldReturnCorrectData() = runTest {
        val weather = WeatherUiModel(
            current = CurrentWeatherUiModel(
                temp = 28,
                feelsLike = 30,
                description = "Sunny",
                iconRes = 100,
                date = "Today",
                cityName = "Riyadh",
                sunrise = "06:00",
                sunset = "18:00",
                main = "Clear"
            ),
            hourly = emptyList(),
            daily = emptyList(),
            extra = ExtraMetricsUiModel(
                pressure = 1012,
                humidity = 50,
                windSpeed = 5.5,
                uvi = 3.0,
                clouds = 10,
                windUnit = "m/s"
            )
        )

        repo.cacheWeather("Riyadh", weather)

        val cached = repo.getCachedWeather()
        assertNotNull(cached)
        assert(cached?.current?.cityName == "Riyadh")
        assert(cached?.current?.temp == 28)
    }

    @Test
    fun settings_shouldUpdateAndReturnValuesCorrectly() {
        repo.setLanguage("ar")
        assert(repo.getLanguage() == "ar")

        repo.setUnitSystem("imperial")
        assert(repo.getUnitSystem() == "imperial")

        repo.setLocationSource("map")
        assert(repo.getLocationSource() == "map")

        repo.setPickedLocation(10.0, 20.0, "Cairo")
        val picked = repo.getPickedLocation()
        assert(picked?.first == 10.0)
        assert(picked?.second == 20.0)
        assert(picked?.third == "Cairo")
    }
}
