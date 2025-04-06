package com.example.weathery.utils

import android.content.Context
import com.example.weathery.data.local.SettingsPreferences
import com.example.weathery.data.local.WeatherDatabase
import com.example.weathery.data.local.WeatherLocalDataSource
import com.example.weathery.data.remote.RetrofitHelper
import com.example.weathery.data.remote.WeatherRemoteDataSource
import com.example.weathery.data.repo.IWeatherRepository
import com.example.weathery.data.repo.WeatherRepository

object RepositoryProvider {
    fun provideRepository(context: Context): IWeatherRepository {
        val dao = WeatherDatabase.getInstance(context).weatherDao()
        val local = WeatherLocalDataSource(dao, SettingsPreferences(context))
        val remote = WeatherRemoteDataSource(RetrofitHelper.service)
        return WeatherRepository(remote, local)
    }
}
