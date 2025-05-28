package com.iti.uc3.forecast.data.network.dataSource

import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.network.api.endpoint.WeatherApiService

open  class WeatherRemoteDataSource (private val apiService: WeatherApiService):IWeatherRemoteDataSource  {


    override  suspend fun getForecastByCoord(lat: Double, lon: Double): WeatherForecastResponse {
        return apiService.getForecastByCoord(lat, lon)
    }

    override  suspend fun getForecastByCityName(cityName: String): WeatherForecastResponse {
        return apiService.getForecastByCityName(cityName)
    }

    override  suspend fun getForecastByCityId(cityId: Long): WeatherForecastResponse {
        return apiService.getForecastByCityId(cityId)
    }


    override  suspend fun getCitySearch(City: String): List<GeoLocation> {
        return apiService.getCitySearch(City)
    }
}