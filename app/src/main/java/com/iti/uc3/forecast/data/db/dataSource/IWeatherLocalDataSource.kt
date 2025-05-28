package com.iti.uc3.forecast.data.db.dataSource

import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.ForecastItemEntity

interface IWeatherLocalDataSource {

        // City-related methods
        suspend fun insertCity(city: CityEntity)

        suspend fun getCity(id: Int): CityEntity?

        suspend fun getAllCities(): List<CityEntity>

        suspend fun getCityByName(name: String): CityEntity?

        suspend fun getFavoriteCities(): List<CityEntity>

        suspend fun deleteCity(id: Int)

        suspend fun updateCityFavorite(id: Int, isFav: Boolean)

        suspend fun clearAllCities()

        // Forecast-related methods
        suspend fun insertForecasts(forecasts: List<ForecastItemEntity>)

        suspend fun deleteForecastById(dt: Long)

        suspend fun getForecastsForCity(cityId: Int): List<ForecastItemEntity>

        suspend fun getForecastByDate(cityId: Int, dt: Long): ForecastItemEntity?

        suspend fun getForecastsForCityInRange(
            cityId: Int,
            startDate: Long,
            endDate: Long
        ): List<ForecastItemEntity>

        suspend fun deleteForecastsForCity(cityId: Int)

        suspend fun clearAllForecasts()
    }