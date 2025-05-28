package com.iti.uc3.forecast.data.model

enum class Language {
    ENGLISH, ARABIC
}
enum class TempUnit {
    CELSIUS, FAHRENHEIT,KELVIN
}
enum class WindSpeedUnit {
    MPS,   // Meters per second (default from APIs like OpenWeather)
    KMPH,  // Kilometers per hour
    MPH    // Miles per hour
}