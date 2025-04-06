package com.example.weathery.data.local


import com.example.weathery.data.models.WeatherUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLocalDataSource : ILocalDataSource {

    private val cities = MutableStateFlow<List<CityEntity>>(emptyList())
    private val alarms = MutableStateFlow<List<WeatherAlarmEntity>>(emptyList())
    private var cachedWeather: WeatherUiModel? = null
    private var alarmIdCounter = 1

    private var language = "en"
    private var unitSystem = "metric"
    private var locationSource = "gps"
    private var pickedLocation: Triple<Double, Double, String>? = null

    override suspend fun insertCity(city: CityEntity) {
        cities.value = cities.value + city
    }

    override fun getAllCities(): Flow<List<CityEntity>> = cities

    override suspend fun deleteCity(city: CityEntity) {
        cities.value = cities.value - city
    }

    override suspend fun insertAlarm(alarm: WeatherAlarmEntity): Int {
        val alarmWithId = alarm.copy(id = alarmIdCounter++)
        alarms.value = alarms.value + alarmWithId
        return alarmWithId.id
    }

    override fun getAllAlarms(): Flow<List<WeatherAlarmEntity>> = alarms

    override suspend fun deleteAlarmById(alarmId: Int) {
        alarms.value = alarms.value.filterNot { it.id == alarmId }
    }

    override suspend fun getAlarmById(id: Int): WeatherAlarmEntity? {
        return alarms.value.find { it.id == id }
    }

    override suspend fun deleteAlarm(alarm: WeatherAlarmEntity) {
        alarms.value = alarms.value - alarm
    }

    override suspend fun deleteExpiredAlarms(currentTime: Long) {
        alarms.value = alarms.value.filterNot { it.endTime < currentTime }
    }

    override suspend fun saveCachedWeather(city: String, weather: WeatherUiModel) {
        cachedWeather = weather
    }

    override suspend fun getCachedWeather(): WeatherUiModel? = cachedWeather

    override fun getLanguage(): String = language
    override fun setLanguage(language: String) {
        this.language = language
    }

    override fun getUnitSystem(): String = unitSystem
    override fun setUnitSystem(system: String) {
        this.unitSystem = system
    }

    override fun getLocationSource(): String = locationSource
    override fun setLocationSource(source: String) {
        this.locationSource = source
    }

    override fun getPickedLocation(): Triple<Double, Double, String>? = pickedLocation
    override fun setPickedLocation(lat: Double, lon: Double, city: String) {
        pickedLocation = Triple(lat, lon, city)
    }
}
