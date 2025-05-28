package com.iti.uc3


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.Coord
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.IWeatherRepository
import com.iti.uc3.forecast.data.repository.WeatherRepository
import com.iti.uc3.forecast.ui.searchcity.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchViewModelTest {

    // Test rules
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher
    private val testDispatcher = StandardTestDispatcher()

    // Mock dependencies
    @Mock
    private lateinit var mockRepository: WeatherRepository

    // System under test
    private lateinit var viewModel: SearchViewModel

    // Mock observers
    @Mock
    private lateinit var citySearchObserver: Observer<List<GeoLocation>>

    @Mock
    private lateinit var fetchStatusObserver: Observer<Boolean>

    // Argument captors
    @Captor
    private lateinit var citySearchCaptor: ArgumentCaptor<List<GeoLocation>>

    @Captor
    private lateinit var fetchStatusCaptor: ArgumentCaptor<Boolean>

    // Test data
    private val testCityName = "Cairo"
    private val testLatitude = 30.0444
    private val testLongitude = 31.2357

    private val testGeoLocations = listOf(
        GeoLocation(
            name = testCityName,
            lat = testLatitude,
            lon = testLongitude,
            country = "EG",
            state = "Cairo"
        )
    )

    private val testCity = CityEntity(
        id = 123,
        name = testCityName,
        coord = Coord(lat = testLatitude, lon = testLongitude),
        country = "EG",
        population = 20000000,
        timezone = 7200,
        sunrise = 1621123456,
        sunset = 1621174567,
        fav = false
    )

    private val testWeatherResponse = WeatherForecastResponse(
        cnt = 1,
        city = testCity,
        list = emptyList()
    )

    @Before
    fun setup() {
        // Set the main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)

        // Initialize ViewModel with mock repository
        viewModel = SearchViewModel(mockRepository)

        // Observe LiveData
        viewModel.currentCityName.observeForever(citySearchObserver)
        viewModel.fetchStatus.observeForever(fetchStatusObserver)
    }

    @After
    fun tearDown() {
        // Remove observers
        viewModel.currentCityName.removeObserver(citySearchObserver)
        viewModel.fetchStatus.removeObserver(fetchStatusObserver)

        // Reset the main dispatcher
        Dispatchers.resetMain()
    }



    @Test
    fun `fetchCityByCoordinates should update fetchStatus with true on success`() = runTest {
        // Arrange
        val successResult = Result.success(testWeatherResponse)
        whenever(mockRepository.getCityByCoordinates(testLatitude, testLongitude)).thenReturn(successResult)

        // Act
        viewModel.fetchCityByCoordinates(testLatitude, testLongitude)
        advanceUntilIdle() // Wait for coroutines to complete

        // Assert
        verify(mockRepository).getCityByCoordinates(testLatitude, testLongitude)
        verify(fetchStatusObserver).onChanged(fetchStatusCaptor.capture())
        assert(fetchStatusCaptor.value == true)
    }

    @Test
    fun `fetchCityByCoordinates should update fetchStatus with false on failure`() = runTest {
        // Arrange
        val failureResult = Result.failure<WeatherForecastResponse>(IOException("Network error"))
        whenever(mockRepository.getCityByCoordinates(testLatitude, testLongitude)).thenReturn(failureResult)

        // Act
        viewModel.fetchCityByCoordinates(testLatitude, testLongitude)
        advanceUntilIdle() // Wait for coroutines to complete

        // Assert
        verify(mockRepository).getCityByCoordinates(testLatitude, testLongitude)
        verify(fetchStatusObserver).onChanged(fetchStatusCaptor.capture())
        assert(fetchStatusCaptor.value == false)
    }



    @Test
    fun `fetchCityByName should update fetchStatus with false on failure`() = runTest {
        // Arrange
        val failureResult = Result.failure<WeatherForecastResponse>(IOException("Network error"))
        whenever(mockRepository.fetchForecastByCityName(testCityName)).thenReturn(failureResult)

        // Act
        viewModel.fetchCityByName(testCityName)
        advanceUntilIdle() // Wait for coroutines to complete

        // Assert
        verify(mockRepository).fetchForecastByCityName(testCityName)
        verify(fetchStatusObserver).onChanged(fetchStatusCaptor.capture())
        assert(fetchStatusCaptor.value == false)
    }

    @Test
    fun `fetchCityByName should not update settings when saveCity is false`() = runTest {
        // Arrange
        val successResult = Result.success(testWeatherResponse)
        whenever(mockRepository.fetchForecastByCityName(testCityName)).thenReturn(successResult)

        // Note: We can't easily test static methods without PowerMockito
        // This test focuses on the repository interaction and LiveData update

        // Act
        viewModel.fetchCityByName(testCityName, false)
        advanceUntilIdle() // Wait for coroutines to complete

        // Assert
        verify(mockRepository).fetchForecastByCityName(testCityName)
        verify(fetchStatusObserver).onChanged(fetchStatusCaptor.capture())
        assert(fetchStatusCaptor.value == true)
    }
}
