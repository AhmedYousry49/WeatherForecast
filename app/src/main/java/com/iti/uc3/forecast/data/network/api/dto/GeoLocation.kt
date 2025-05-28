package com.iti.uc3.forecast.data.network.api.dto

import com.google.gson.annotations.SerializedName


data class GeoLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String?= null,
    val state: String?= null,
    @SerializedName("local_names") val localNames: Map<String, String>? = null
)