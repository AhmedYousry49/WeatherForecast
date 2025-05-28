package com.iti.uc3.forecast.alerts


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.repository.WeatherRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the WeatherAlertFragment
 */
class WeatherAlertViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _cities = MutableLiveData<List<CityEntity>>()
    val cities: LiveData<List<CityEntity>> = _cities

    /**
     * Load all saved cities from the repository
     */
    fun loadSavedCities() {
        viewModelScope.launch {
            try {
                val savedCities = repository.readAllCity()
                if( savedCities.isSuccess) {
                    // If no cities are saved, you might want to handle this case
                    _cities.value = savedCities.getOrDefault(emptyList()) as List<CityEntity>?
                }

            } catch (e: Exception) {
                // Handle error
                _cities.value = emptyList()
            }
        }
    }

    /**
     * Check if an alert is already scheduled for a city
     */
    fun isAlertScheduledForCity(cityId: Int): Boolean {
        // This would typically check WorkManager for existing work
        // For demonstration purposes, we'll return false
        return false
    }

    /**
     * Save alert preferences for a city
     */
    fun saveAlertPreferences(
        cityId: Int,
        hour: Int,
        minute: Int,
        days: Set<Int>,
        alertRain: Boolean,
        alertSnow: Boolean,
        alertExtremeTemp: Boolean,
        alertWind: Boolean
    ) {
        viewModelScope.launch {
            try {
                // Save preferences to SharedPreferences or database
                // This is a placeholder for actual implementation
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
