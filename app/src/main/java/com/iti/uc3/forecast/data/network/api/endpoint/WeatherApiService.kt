package com.iti.uc3.forecast.data.network.api.endpoint

import com.iti.uc3.forecast.BuildConfig
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/forecast")
    suspend fun getForecastByCoord(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): WeatherForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCityName (
        @Query("q") lat: String,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): WeatherForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCityId(
        @Query("id") lon: Long,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): WeatherForecastResponse


    @GET("geo/1.0/direct")
    suspend fun getCitySearch(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 15,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): List<GeoLocation>

}