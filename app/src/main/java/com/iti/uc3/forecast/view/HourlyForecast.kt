package com.iti.uc3.forecast.view

data class HourlyForecast(
    val hour: String, // e.g. "01:00"
    val temperature: Int // e.g. 22 (°C or °F)
)