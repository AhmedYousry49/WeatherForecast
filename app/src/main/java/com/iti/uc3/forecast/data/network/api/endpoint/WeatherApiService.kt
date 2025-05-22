package com.iti.uc3.forecast.data.network.api.endpoint

import com.iti.uc3.forecast.BuildConfig
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/forecast")
    suspend fun getForecastbycoord(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): WeatherForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastbyCitynmae(
        @Query("q") lat: String,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): WeatherForecastResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastbyCityid(
        @Query("id") lon: Long,
        @Query("appid") apiKey: String= BuildConfig.Api_Key
    ): WeatherForecastResponse

}