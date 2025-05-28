package com.iti.uc3.forecast.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iti.uc3.forecast.data.model.CityEntity

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getCitybyId(id: Int): CityEntity?

    @Query("SELECT * FROM cities WHERE name = :name")
    suspend fun getCitybyName(name: String): CityEntity?

    @Query("SELECT * FROM cities WHERE fav = 1")
    suspend fun getFavCities(): List<CityEntity>

    @Query("SELECT * FROM cities")
    suspend fun getAllCities(): List<CityEntity>

    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCity(id: Int)
    @Query("UPDATE cities SET fav = :isFav WHERE id = :id")
    suspend fun updateCityFav(id: Int, isFav: Boolean)
    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()


    @Query("SELECT COUNT(*) FROM cities WHERE id = :id")
    suspend fun isCityExists(id: Int): Int

}