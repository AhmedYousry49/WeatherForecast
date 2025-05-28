package com.iti.uc3.forecast.ui.searchcity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.WeatherRepository
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import kotlinx.coroutines.launch

open class SearchViewModel(private val repository: WeatherRepository) : ViewModel() {


    private val _currentCityName = MutableLiveData<List<GeoLocation>>()
    val currentCityName: LiveData<List<GeoLocation> > get() = _currentCityName


    fun  CitySearch (query: String)
    {
        viewModelScope.launch {
            _currentCityName.value= repository.getCitySearch(query)

        }

    }
    private val _fetchStatus = MutableLiveData<Boolean>() // true = success, false = error
    val fetchStatus: LiveData<Boolean> get() = _fetchStatus

    fun fetchCityByCoordinates(latitude: Double, longitude: Double,setCurrentCity:Boolean=false) {
        viewModelScope.launch {
            val result = repository.getCityByCoordinates(latitude, longitude)


            if(result.isSuccess) {

                if(setCurrentCity)
                {
                    SettingsPrefs.setCurrentCity(result.getOrNull()?.city?.name ?: "")
                    SettingsPrefs.setFirstTime(false)
                }
                _fetchStatus.value =true

            }
            else
                _fetchStatus.value =false


        }
    }

    fun fetchCityByName(name: String,saveCity:Boolean= true) {
        viewModelScope.launch {
            val result = repository.fetchForecastByCityName(name)
            if(result.isSuccess) {
                _fetchStatus.postValue(true)
                if(saveCity) {

                    SettingsPrefs.setCurrentCity(result.getOrNull()?.city?.name ?: "")
                    SettingsPrefs.setFirstTime(false)
                }
            }
            else
                _fetchStatus.postValue(false)

        }
    }


    }