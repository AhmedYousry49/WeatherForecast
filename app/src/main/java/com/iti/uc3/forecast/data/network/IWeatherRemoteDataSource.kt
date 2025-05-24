package com.iti.uc3.forecast.data.network

import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

interface IWeatherRemoteDataSource {

    fun getCurrentWeatherby(lat: Double,lon: Double) : Flow<WeatherForecastResponse>


}