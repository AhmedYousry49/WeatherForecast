package com.iti.uc3.forecast.data.model

data class DailyTemperatureRange(
    val dayName: String,         // e.g., "Monday"
    val date: String,            // Original date string, e.g., "2025-05-26"
    val tempMin: String,
    val tempMax: String,
    val description: String,     // e.g., "Light rain"
    val icon: String
)