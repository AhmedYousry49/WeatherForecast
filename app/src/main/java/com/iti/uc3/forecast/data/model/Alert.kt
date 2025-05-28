package com.iti.uc3.forecast.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityId: Int?,
    val latitude: Double,
    val longitude: Double,
    val dateTime: Long,
    val type: String
)