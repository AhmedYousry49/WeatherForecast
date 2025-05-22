package com.iti.uc3.forecast.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iti.uc3.forecast.data.model.ForecastItemEntity

@Dao
interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<ForecastItemEntity>)

    @Query("SELECT * FROM forecast_items WHERE cityId = :cityId ORDER BY dt ASC")
    suspend fun getForecastsForCity(cityId: Int): List<ForecastItemEntity>

    @Query("DELETE FROM forecast_items WHERE cityId = :cityId")
    suspend fun deleteForecastsForCity(cityId: Int)

    @Query("DELETE FROM forecast_items")
    suspend fun deleteAllForecasts()
    @Query("SELECT * FROM forecast_items WHERE dt = :dt AND cityId = :cityId")
    suspend fun getForecastByDateAndCityId(dt: Long, cityId: Int): ForecastItemEntity?
    @Query("SELECT * FROM forecast_items WHERE cityId = :cityId AND dt BETWEEN :startDate AND :endDate")
    suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<ForecastItemEntity>


}