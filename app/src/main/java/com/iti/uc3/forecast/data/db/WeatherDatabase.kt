package com.iti.uc3.forecast.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iti.uc3.forecast.data.db.dao.CityDao
import com.iti.uc3.forecast.data.db.dao.ForecastDao
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.ForecastItemEntity

@Database(
    entities = [CityEntity::class, ForecastItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao//
    abstract fun forecastDao(): ForecastDao //

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }


}
