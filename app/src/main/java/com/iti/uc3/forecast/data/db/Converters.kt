package com.iti.uc3.forecast.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.iti.uc3.forecast.data.model.WeatherCondition

class Converters {
    @TypeConverter
    fun fromWeatherList(value: List<WeatherCondition>): String =
        Gson().toJson(value)

    @TypeConverter
    fun toWeatherList(value: String): List<WeatherCondition> =
        Gson().fromJson(value, Array<WeatherCondition>::class.java).toList()
}