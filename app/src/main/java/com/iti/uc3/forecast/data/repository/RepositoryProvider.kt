package com.iti.uc3.forecast.data.repository

import android.content.Context
import com.iti.uc3.forecast.data.db.WeatherDatabase
import com.iti.uc3.forecast.data.db.dataSource.LocalDataSource
import com.iti.uc3.forecast.data.network.api.client.RetrofitClient
import com.iti.uc3.forecast.data.network.dataSource.WeatherRemoteDataSource

object RepositoryProvider {

    @Volatile
    private var INSTANCE: WeatherRepository? = null

    fun getRepository(context: Context): WeatherRepository {
        return INSTANCE ?: synchronized(this) {
            val database = WeatherDatabase.getInstance(context)
            val localDataSource = LocalDataSource(cityDao = database.cityDao(),forecastDao = database.forecastDao())
            val remoteDataSource = WeatherRemoteDataSource(RetrofitClient.weatherApi)
            val instance = WeatherRepository(remoteDataSource, localDataSource)
            INSTANCE = instance
            instance
        }
    }
}