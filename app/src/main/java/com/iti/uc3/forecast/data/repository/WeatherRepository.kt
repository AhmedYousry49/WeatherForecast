package com.iti.uc3.forecast.data.repository

import android.util.Log
import androidx.work.Data
import com.iti.uc3.forecast.data.network.dataSource.IWeatherRemoteDataSource
import com.iti.uc3.forecast.data.db.dataSource.IWeatherLocalDataSource
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.ForecastItemEntity
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

fun CityEntity.toCityEntity(fav: Boolean = false): CityEntity {
    return CityEntity(
        id = this.id,
        name = this.name,
        country = this.country,
        population = this.population,
        timezone = this.timezone,
        sunrise = this.sunrise,
        sunset = this.sunset,
        coord = this.coord,
        fav = fav
    )
}
class WeatherRepository (private val remoteDataSource:IWeatherRemoteDataSource,private val localDataSource:IWeatherLocalDataSource ) : IWeatherRepository {
    fun ForecastItemEntity.toForecastItemEntity(cityId: Int): ForecastItemEntity {
        return ForecastItemEntity(
            id = 0, // 0 or leave it to autoGenerate by Room
            dt = this.dt,
            cityId = cityId,
            main = this.main,
            weather = this.weather,
            clouds = this.clouds,
            wind = this.wind,
            visibility = this.visibility,
            pop = this.pop,
            sys = this.sys,
            dt_txt = this.dt_txt
        )
    }


    override  suspend fun fetchForecastByCityName(cityName: String): Result<WeatherForecastResponse> = withContext(Dispatchers.IO) {
        try {
            val response = remoteDataSource.getForecastByCityName(cityName)

            val cityEntity = response.city.toCityEntity()
            val forecastEntities = response.list.map { it.toForecastItemEntity(cityEntity.id) }

            val todayStartTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis/1000
            Log.d("WeatherRepository", "Today's start time: $todayStartTime")
            localDataSource.deleteForecastById(todayStartTime) // Clear old forecasts before inserting new ones
            localDataSource.insertCity(cityEntity)
            localDataSource.insertForecasts(forecastEntities)
            Log.d("WeatherRepository", "Forecast fetched and saved successfully for city: $cityName")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching forecast: ${e.message}")
            Result.failure(e)
        }
    }
    override  suspend fun getCityByCoordinates(latitude: Double, longitude: Double): Result<WeatherForecastResponse> = withContext(Dispatchers.IO) {
        try {
            val response = remoteDataSource.getForecastByCoord(latitude,longitude)

            val cityEntity = response.city.toCityEntity()
            val forecastEntities = response.list.map { it.toForecastItemEntity(cityEntity.id) }

            val todayStartTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis/1000
            Log.d("WeatherRepository", "Today's start time: $todayStartTime")
            localDataSource.deleteForecastById(todayStartTime) // Clear old forecasts before inserting new ones
            localDataSource.insertCity(cityEntity)
            localDataSource.insertForecasts(forecastEntities)
            Log.d("WeatherRepository", "Forecast fetched and saved successfully for city: $cityEntity.name")

          return@withContext  Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching forecast: ${e.message}")
            return@withContext   Result.failure(e)
        }
    }
    override   suspend fun getForecastByCityid(cityid: Int):WeatherForecastResponse? = withContext(Dispatchers.IO) {


        try {


            var city = localDataSource.getCity(cityid)
            if(city!=null) {
                var list = localDataSource.getForecastsForCity(city!!.id)
                return@withContext WeatherForecastResponse(
                    list.size,
                    city = city,
                    list = list
                )
            }
            else {
                Log.e("WeatherRepository", "City not found in local database: ")
                return@withContext null // Return null if city not found
            }
        }
        catch (
            e: Exception
        ) {
            Log.e("WeatherRepository", "Error fetching forecast by city name: ${e.message}")
            throw e // Re-throw the exception to be handled by the caller
        }

    }
  override  suspend fun getForecastByCityName(cityName: String):WeatherForecastResponse? = withContext(Dispatchers.IO) {
//     try {
//         //fetchForecastByCityName(cityName) // Fetch forecast and save to local DB
//     }
//        catch (e: Exception) {
//            Log.e("WeatherRepository", "Error fetching forecast by city name: ${e.message}")
//            return@withContext null // Return null if there's an error
//        }

        try {


           var city = localDataSource.getCityByName(cityName)
            if(city!=null) {
                var list = localDataSource.getForecastsForCity(city!!.id)
                return@withContext WeatherForecastResponse(
                    list.size,
                    city = city,
                    list = list
                )
            }
            else {
                Log.e("WeatherRepository", "City not found in local database: $cityName")
                return@withContext null // Return null if city not found
            }
        }
        catch (
            e: Exception
        ) {
            Log.e("WeatherRepository", "Error fetching forecast by city name: ${e.message}")
            throw e // Re-throw the exception to be handled by the caller
        }

    }

    override   suspend fun updateDatabase(): Result<Boolean> = withContext(Dispatchers.IO)
    {
        try {
            val cities = localDataSource.getAllCities()
            if (cities.isEmpty()) {
                Log.d("WeatherRepository", "No cities found in local database.")
                return@withContext Result.failure(Exception("No cities found in local database."))
            }

      cities.forEach { fetchForecastByCityName( it.name) }

            return@withContext Result.success(true)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error updating database: ${e.message}")
            return@withContext Result.failure(Exception("Error updating database: ${e.message}"))
        }
    }

    override   suspend fun getAllCityName(): Result<List<String>> = withContext(Dispatchers.IO)
    {

        try {
            val cities = localDataSource.getAllCities()
            if (cities.isEmpty()) {
                Log.d("WeatherRepository", "No cities found in local database.")
                return@withContext Result.failure(Exception("No cities found in local database."))
            }
            val cityNames = cities.map { it.name }
            Log.d("WeatherRepository", "Cities found in local database: ${cityNames.size}")
            return@withContext   Result.success(cityNames)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching city names: ${e.message}")
            return@withContext Result.failure(Exception("Error fetching city names: ${e.message}"))
        }
    }

    override  suspend fun deleteCity(cityId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete forecasts for the city first
            localDataSource.deleteForecastsForCity(cityId)
            // Then delete the city itself
            localDataSource.deleteCity(cityId)
            Log.d("WeatherRepository", "City with ID $cityId deleted successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error deleting city: ${e.message}")
            Result.failure(e)
        }


    }

    override       suspend fun readAllCity(): Result<List<WeatherForecastResponse>> = withContext(Dispatchers.IO) {
        val getAllCities = localDataSource.getAllCities()

        if (getAllCities.isEmpty()) {
            Log.d("WeatherRepository", "No cities found in local database.")
            return@withContext Result.failure(Exception("No cities found"))
        }

        val allWeather = mutableListOf<WeatherForecastResponse>()
        for (city in getAllCities) {
            val forecasts = localDataSource.getForecastsForCity(city.id)
            if (forecasts.isNotEmpty()) {
                // Normally you wouldn’t create new WeatherRepository per city,
                // but if that's your intent, this line is okay.
                WeatherForecastResponse(
                    cnt = forecasts.size,
                    city = city,
                    list = forecasts
                ).also { allWeather.add(it) }

            } else {
                Log.d("WeatherRepository", "No forecasts found for city: ${city.name}")
            }
        }

        Log.d("WeatherRepository", "Cities found in local database: ${getAllCities.size}")
        return@withContext Result.success(allWeather)
    }
    override    suspend fun  getCitySearch(City: String):List<GeoLocation> = withContext(Dispatchers.IO)
    {

        return@withContext  remoteDataSource.getCitySearch(City)
    }



//    suspend fun fetchForecastByCityName(cityName: String) {
//        withContext(Dispatchers.IO) {
//            val response = remoteDataSource.getForecastByCityName(cityName)
//
//            // Convert API response to entities
//            val cityEntity = response.city
//            val forecastEntities = response.list.map { it.toForecastItemEntity(cityEntity.id) }
//
//            // Save data locally
//            localDataSource.insertCity(cityEntity)
//            localDataSource.deleteForecastsForCity(cityEntity.id)  // Clear old forecasts for city
//            localDataSource.insertForecasts(forecastEntities)
//        }
//
//    }

    // Get forecast from local DB by city ID
    override   suspend fun getForecastsForCity(cityId: Int): List<ForecastItemEntity> {
        return localDataSource.getForecastsForCity(cityId)
    }

    companion object {
        private var instance: WeatherRepository? = null

        fun getInstance(
            weatherRemoteDataSource: IWeatherRemoteDataSource,
            weatherLocalDataSource: IWeatherLocalDataSource
        ): WeatherRepository {
            return instance ?: synchronized(this) {
                val temp = WeatherRepository(weatherRemoteDataSource, weatherLocalDataSource)
                instance = temp
                temp
            }
        }
    }

    // Implement methods from IWeatherRepository here


}