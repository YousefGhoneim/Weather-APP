package com.example.weathery.data.local

class FakeAppPreferences : ISettingsPreferences {

    private var lang = "en"
    private var unit = "metric"
    private var source = "gps"
    private var pickedLocation: Triple<Double, Double, String>? = null

    override fun getLanguage(): String = lang
    override fun setLanguage(language: String) { lang = language }

    override fun getUnitSystem(): String = unit
    override fun setUnitSystem(system: String) { unit = system }

    override fun getLocationSource(): String = source
    override fun setLocationSource(source: String) { this.source = source }

    override fun getPickedLocation(): Triple<Double, Double, String>? = pickedLocation
    override fun setPickedLocation(lat: Double, lon: Double, city: String) {
        pickedLocation = Triple(lat, lon, city)
    }
}
