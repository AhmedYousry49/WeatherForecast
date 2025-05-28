package com.iti.uc3.forecast


import com.iti.uc3.forecast.data.db.dataSource.IWeatherLocalDataSource
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.Clouds
import com.iti.uc3.forecast.data.model.Coord
import com.iti.uc3.forecast.data.model.ForecastItemEntity
import com.iti.uc3.forecast.data.model.MainWeather
import com.iti.uc3.forecast.data.model.Sys
import com.iti.uc3.forecast.data.model.WeatherCondition
import com.iti.uc3.forecast.data.model.Wind
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.network.dataSource.IWeatherRemoteDataSource
import com.iti.uc3.forecast.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Main dispatcher rule for testing coroutines
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class WeatherRepositoryTest {

    // Test dispatcher rule for controlling coroutines
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Test dispatcher for controlling coroutines
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Mock dependencies
    private lateinit var mockRemoteDataSource: IWeatherRemoteDataSource
    private lateinit var mockLocalDataSource: IWeatherLocalDataSource

    // System under test
    private lateinit var weatherRepository: WeatherRepository

    // Test data
    private val testCityName = "Cairo"
    private val testCityId = 123
    private val testLatitude = 30.0444
    private val testLongitude = 31.2357

    private val testCity = CityEntity(
        id = testCityId,
        name = testCityName,
        coord = Coord(lat = testLatitude, lon = testLongitude),
        country = "EG",
        population = 20000000,
        timezone = 7200,
        sunrise = 1621123456,
        sunset = 1621174567,
        fav = false
    )

    private val testForecastItem = ForecastItemEntity(
        id = 1,
        dt = 1621123456,
        cityId = testCityId,
        main = MainWeather(
            temp = 30.5,
            feels_like = 32.0,
            temp_min = 28.0,
            temp_max = 33.0,
            pressure = 1012,
            sea_level = 1012,
            grnd_level = 1010,
            humidity = 45,
            temp_kf = 0.0
        ),
        weather = listOf(WeatherCondition(id = 800, main = "Clear", description = "clear sky", icon = "01d")),
        clouds = Clouds(5),
        wind = Wind(
            speed = 3.5,
            deg = 180,
            gust = 4.0
        ),
        visibility = 10000,
        pop = 0.0,
        sys = Sys(
            pod = "d"
        ),
        dt_txt = "2025-05-28 12:00:00"
    )

    private val testForecastList = listOf(testForecastItem)

    private val testWeatherResponse = WeatherForecastResponse(
        cnt = 1,
        city = testCity,
        list = testForecastList
    )

    private val testGeoLocations = listOf(
        GeoLocation(
            name = testCityName,
            lat = testLatitude,
            lon = testLongitude,
            country = "EG",
            state = "Cairo"
        )
    )

    @Before
    fun setup() {
        // Initialize mocks
        mockRemoteDataSource = mock()
        mockLocalDataSource = mock()

        // Initialize repository with mocks
        weatherRepository = WeatherRepository(mockRemoteDataSource, mockLocalDataSource)
    }

    @Test
    fun `getForecastByCityId should return weather response when city exists in local database`() = testScope.runTest {
        // Arrange
        whenever(mockLocalDataSource.getCity(testCityId)).thenReturn(testCity)
        whenever(mockLocalDataSource.getForecastsForCity(testCityId)).thenReturn(testForecastList)

        // Act
        val result = weatherRepository.getForecastByCityid(testCityId)
        advanceUntilIdle()

        // Assert
        assertNotNull(result)
        assertEquals(testCity, result?.city)
        assertEquals(testForecastList, result?.list)

        // Verify interactions
        verify(mockLocalDataSource).getCity(testCityId)
        verify(mockLocalDataSource).getForecastsForCity(testCityId)
    }


    @Test
    fun `getForecastByCityName should return weather response when city exists in local database`() = testScope.runTest {
        // Arrange
        whenever(mockLocalDataSource.getCityByName(testCityName)).thenReturn(testCity)
        whenever(mockLocalDataSource.getForecastsForCity(testCityId)).thenReturn(testForecastList)

        // Act
        val result = weatherRepository.getForecastByCityName(testCityName)
        advanceUntilIdle()

        // Assert
        assertNotNull(result)
        assertEquals(testCity, result?.city)
        assertEquals(testForecastList, result?.list)

        // Verify interactions
        verify(mockLocalDataSource).getCityByName(testCityName)
        verify(mockLocalDataSource).getForecastsForCity(testCityId)
    }

    @Test
    fun `getCitySearch should return geo locations from remote data source`() = testScope.runTest {
        // Arrange
        whenever(mockRemoteDataSource.getCitySearch(testCityName)).thenReturn(testGeoLocations)

        // Act
        val result = weatherRepository.getCitySearch(testCityName)
        advanceUntilIdle()

        // Assert
        assertEquals(testGeoLocations, result)

        // Verify interactions
        verify(mockRemoteDataSource).getCitySearch(testCityName)
    }

    @Test
    fun `getForecastsForCity should return forecasts from local data source`() = testScope.runTest {
        // Arrange
        whenever(mockLocalDataSource.getForecastsForCity(testCityId)).thenReturn(testForecastList)

        // Act
        val result = weatherRepository.getForecastsForCity(testCityId)
        advanceUntilIdle()

        // Assert
        assertEquals(testForecastList, result)

        // Verify interactions
        verify(mockLocalDataSource).getForecastsForCity(testCityId)
    }
}
