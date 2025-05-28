package com.iti.uc3.forecast.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.iti.uc3.LocaleHelper.convertWindSpeed
import com.iti.uc3.forecast.data.model.Coord
import com.iti.uc3.forecast.data.model.Language
import com.iti.uc3.forecast.data.model.Settings
import com.iti.uc3.forecast.data.model.TempUnit
import com.iti.uc3.forecast.data.model.WindSpeedUnit
import kotlin.math.roundToInt

object SettingsPrefs {

    private const val PREF_NAME = "app_settings"
    private const val KEY_SETTINGS = "settings"
    private const val KEY_FIRST_TIME = "first_time"
    private val gson = Gson()
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
         prefs = PreferenceManager.getDefaultSharedPreferences(context)

       // prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getLang():String {

      return  prefs.getString("language", "en") ?: "en"
    }


    fun getTempUnit(): TempUnit {
        val value = prefs.getString("temp_unit", TempUnit.CELSIUS.name)
        return try {
            enumValueOf<TempUnit>(value ?: TempUnit.CELSIUS.name)
        } catch (e: IllegalArgumentException) {
            TempUnit.CELSIUS
        }
    }

    fun saveSettings(settings: Settings) {
        val json = gson.toJson(settings)
        prefs.edit().putString(KEY_SETTINGS, json).apply()
    }
    fun isFirstTime(): Boolean {
        return prefs.getBoolean(KEY_FIRST_TIME, true)
    }
    fun setFirstTime(value: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_TIME, value).apply()
    }
    fun defaultSettings(): Settings {
        return Settings(
            language = Language.ENGLISH,
            tempUnit = TempUnit.CELSIUS,
            windSpeedUnit = WindSpeedUnit.MPS,
            isManualLocation = false,
            coordinates = Coord(0.0, 0.0)
        )
    }

    fun setDefaultSettings() {
        val default = defaultSettings()
        saveSettings(default)
        setFirstTime(false)
    }



    fun getSettings(): Settings {
        val json = prefs.getString(KEY_SETTINGS, null)
        return if (json != null) {
            gson.fromJson(json, Settings::class.java)
        } else {
            defaultSettings()
        }
    }

    fun isnotificationAgreed(): Boolean {
        // Check if the user has agreed to notifications
        // Default to false if not set
       return prefs.getBoolean("WeatherAppPrefs", false)
    }

    fun setnotificationAgreed(value: Boolean) {

        prefs.edit().putBoolean("WeatherAppPrefs", value).apply()

    }
    fun getWindSpeedUnit(): WindSpeedUnit {
        val value = prefs.getString("wind_speed_unit", WindSpeedUnit.MPS.name)
        return try {
            enumValueOf<WindSpeedUnit>(value ?: WindSpeedUnit.MPS.name)
        } catch (e: IllegalArgumentException) {
            WindSpeedUnit.MPS
        }
    }

    fun getCurrentCity(): String {
        return prefs.getString("current_city", "Cairo") ?: "Cairo"
    }
    fun setCurrentCity(city: String) {
        prefs.edit().putString("current_city", city).apply()
    }

}