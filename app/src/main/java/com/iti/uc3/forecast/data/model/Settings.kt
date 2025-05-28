package com.iti.uc3.forecast.data.model

data class Settings(
    val language: Language,
    val tempUnit: TempUnit,
    val windSpeedUnit: WindSpeedUnit,
    val isManualLocation: Boolean,
    val coordinates: Coord
)