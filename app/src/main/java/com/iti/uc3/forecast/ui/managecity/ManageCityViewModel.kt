package com.iti.uc3.forecast.ui.managecity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class ManageCityViewModel (private val repository: WeatherRepository) : ViewModel() {


    private val _currentWeather = MutableLiveData<List<WeatherForecastResponse>>()
    val currentWeather: LiveData<List<WeatherForecastResponse>> get() = _currentWeather



    fun readAllCity() {
        viewModelScope.launch {
            val result= repository.readAllCity()
            if (result.isSuccess) {
                _currentWeather.value = result.getOrNull()
            }
        }


    }

    fun deleteCities(strings: List<Int>)
    {

        viewModelScope.launch {
            for (cityId in strings) {
                repository.deleteCity(cityId)
            }
            readAllCity() // Refresh the list after deletion
        }



    }


}