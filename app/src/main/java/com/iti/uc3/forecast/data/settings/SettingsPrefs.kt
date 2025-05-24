package com.iti.uc3.forecast.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.iti.uc3.forecast.data.model.Coord
import com.iti.uc3.forecast.data.model.Language
import com.iti.uc3.forecast.data.model.Settings
import com.iti.uc3.forecast.data.model.TempUnit
import com.iti.uc3.forecast.data.model.WindSpeedUnit

object SettingsPrefs {

    private const val PREF_NAME = "app_settings"
    private const val KEY_SETTINGS = "settings"
    private const val KEY_FIRST_TIME = "first_time"
    private val gson = Gson()
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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
            windSpeedUnit = WindSpeedUnit.KMH,
            isManualLocation = false,
            coordinates = Coord(0.0, 0.0)
        )
    }

    fun getSettings(): Settings {
        val json = prefs.getString(KEY_SETTINGS, null)
        return if (json != null) {
            gson.fromJson(json, Settings::class.java)
        } else {
            defaultSettings()
        }
    }
}