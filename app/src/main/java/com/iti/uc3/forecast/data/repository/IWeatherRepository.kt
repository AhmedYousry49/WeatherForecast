package com.iti.uc3.forecast.data.repository

import com.iti.uc3.forecast.data.model.ForecastItemEntity
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse

interface IWeatherRepository {
    suspend fun fetchForecastByCityName(cityName: String): Result<WeatherForecastResponse>
    suspend fun getCityByCoordinates(latitude: Double, longitude: Double): Result<WeatherForecastResponse>
    suspend fun getForecastByCityid(cityId: Int): WeatherForecastResponse?
    suspend fun getForecastByCityName(cityName: String): WeatherForecastResponse?
    suspend fun updateDatabase(): Result<Boolean>
    suspend fun getAllCityName(): Result<List<String>>
    suspend fun deleteCity(cityId: Int): Result<Unit>
    suspend fun readAllCity(): Result<List<WeatherForecastResponse>>
    suspend fun getCitySearch(City: String): List<GeoLocation>
    suspend fun getForecastsForCity(cityId: Int): List<ForecastItemEntity>
}