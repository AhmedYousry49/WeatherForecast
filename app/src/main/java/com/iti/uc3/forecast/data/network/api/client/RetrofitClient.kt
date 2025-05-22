package com.iti.uc3.forecast.data.network.api.client

import com.iti.uc3.forecast.data.network.api.endpoint.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient  {

    private const val BASE_URL = "https://api.openweathermap.org/"

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}