package com.iti.uc3.forecast.data.model

data class Coord(
    val lat: Double,
    val lon: Double
)

data class MainWeather(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val sea_level: Int,
    val grnd_level: Int,
    val humidity: Int,
    val temp_kf: Double
)

data class Clouds(val all: Int)

data class Wind(val speed: Double, val deg: Int, val gust: Double)

data class Sys(val pod: String)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)