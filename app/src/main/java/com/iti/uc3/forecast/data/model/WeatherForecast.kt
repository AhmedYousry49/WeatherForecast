
package com.iti.uc3.forecast.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.iti.uc3.forecast.data.db.Converters

@Entity(
    tableName = "forecast_items",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cityId")]
)
data class ForecastItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var dt: Long,
    var cityId: Int, // FK reference to CityEntity
    @Embedded(prefix = "main_") var main: MainWeather,
    @TypeConverters(Converters::class) var weather: List<WeatherCondition>,
    @Embedded(prefix = "clouds_") var clouds: Clouds,
    @Embedded(prefix = "wind_") var wind: Wind,
    var visibility: Int,
    var pop: Double,
    @Embedded(prefix = "sys_") var sys: Sys,
    var dt_txt: String
)