package com.iti.uc3.forecast.data.db.dataSource

import com.iti.uc3.forecast.data.db.dataSource.IWeatherLocalDataSource
import com.iti.uc3.forecast.data.db.dao.CityDao
import com.iti.uc3.forecast.data.db.dao.ForecastDao
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.ForecastItemEntity

open class LocalDataSource(
    private val cityDao: CityDao,
    private val forecastDao: ForecastDao
): IWeatherLocalDataSource


{
//    suspend fun getCitySafely(id: Int): Result<CityEntity?> = runCatching {
//        cityDao.getCity(id)
//    }
    // --- City-related methods ---

    override  suspend fun insertCity(city: CityEntity) = cityDao.insertCity(city)

    override  suspend fun getCity(id: Int): CityEntity? = cityDao.getCitybyId(id)

    override suspend fun getCityByName(name: String): CityEntity? = cityDao.getCitybyName(name)

    override suspend fun getAllCities(): List<CityEntity> = cityDao.getAllCities()

    override suspend fun getFavoriteCities(): List<CityEntity> = cityDao.getFavCities()


    override suspend fun deleteCity(id: Int) = cityDao.deleteCity(id)

    override suspend fun updateCityFavorite(id: Int, isFav: Boolean) = cityDao.updateCityFav(id, isFav)

    override suspend fun clearAllCities() = cityDao.deleteAllCities()

    // --- Forecast-related methods ---

    override suspend fun insertForecasts(forecasts: List<ForecastItemEntity>) =
        forecastDao.insertForecasts(forecasts)

    override suspend fun getForecastsForCity(cityId: Int): List<ForecastItemEntity> =
        forecastDao.getForecastsForCity(cityId)

    override suspend fun getForecastByDate(cityId: Int, dt: Long): ForecastItemEntity? =
        forecastDao.getForecastByDateAndCityId(dt, cityId)

    override suspend fun getForecastsForCityInRange(
        cityId: Int,
        startDate: Long,
        endDate: Long
    ): List<ForecastItemEntity> =
        forecastDao.getForecastsForCityInRange(cityId, startDate, endDate)

    override  suspend fun deleteForecastsForCity(cityId: Int) =
        forecastDao.deleteForecastsForCity(cityId)

    override suspend fun clearAllForecasts() = forecastDao.deleteAllForecasts()


    override suspend fun deleteForecastById(id: Long) = forecastDao.deleteForecastLastTime(id)
}