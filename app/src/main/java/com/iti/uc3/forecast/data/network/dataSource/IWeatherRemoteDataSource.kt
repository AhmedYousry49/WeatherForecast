package com.iti.uc3.forecast.data.network.dataSource

import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

interface IWeatherRemoteDataSource {

    suspend fun getForecastByCoord(lat: Double, lon: Double): WeatherForecastResponse

    suspend fun getForecastByCityName(cityName: String): WeatherForecastResponse

    suspend fun getForecastByCityId(cityId: Long): WeatherForecastResponse

    suspend fun getCitySearch(City: String): List<GeoLocation>


}