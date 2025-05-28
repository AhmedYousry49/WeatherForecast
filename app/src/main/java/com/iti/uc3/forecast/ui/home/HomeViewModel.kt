package com.iti.uc3.forecast.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.WeatherRepository
import kotlinx.coroutines.launch


class HomeViewModel (private val repository: WeatherRepository) : ViewModel() {


    private val _currentWeather = MutableLiveData<WeatherForecastResponse>()
    val currentWeather: LiveData<WeatherForecastResponse> get() = _currentWeather



    fun getWeatherByname(name: String) {
        viewModelScope.launch {
            val result= repository.getForecastByCityName(name)
            if (result != null) {
                _currentWeather.value = result
            }
        }




    }

    fun UpdateDatebase() {
        viewModelScope.launch {
            repository.updateDatabase()
        }
    }

    // TODO: Implement the ViewModel
}