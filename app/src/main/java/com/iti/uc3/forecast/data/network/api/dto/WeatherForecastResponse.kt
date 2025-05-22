package com.iti.uc3.forecast.data.network.api.dto

import com.google.gson.annotations.SerializedName
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.ForecastItemEntity

data class WeatherForecastResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<ForecastItemEntity>,
    @SerializedName("city") val city: CityEntity
)