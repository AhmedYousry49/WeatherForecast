package com.iti.uc3

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.preference.PreferenceManager
import com.iti.uc3.forecast.data.model.TempUnit
import com.iti.uc3.forecast.data.model.WindSpeedUnit
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import java.util.Locale
import kotlin.math.log
import kotlin.math.roundToInt

object LocaleHelper {

    fun convertWindSpeed(speedInMps: Double, toUnit: WindSpeedUnit = SettingsPrefs.getWindSpeedUnit()): Double {
        return when (toUnit) {
            WindSpeedUnit.MPS -> speedInMps
            WindSpeedUnit.KMPH -> speedInMps * 3.6
            WindSpeedUnit.MPH -> speedInMps * 2.23694
        }
    }


    fun convertTemperature(valueInKelvin: Int, toUnit: TempUnit=SettingsPrefs.getTempUnit()): Int {
        return when (toUnit) {
            TempUnit.CELSIUS -> (valueInKelvin - 273.15).toInt()
            TempUnit.FAHRENHEIT -> ((valueInKelvin - 273.15) * 9/5 + 32).toInt()
            TempUnit.KELVIN -> valueInKelvin.toInt()
        }
    }
    fun convertformatTemperature(valueInKelvin: Int, toUnit: TempUnit=SettingsPrefs.getTempUnit()): String {
        return formatTemperature(convertTemperature(valueInKelvin, toUnit), toUnit)
    }

    fun formatWindSpeed(speed: Double, unit: WindSpeedUnit = SettingsPrefs.getWindSpeedUnit()): String {
        val symbol = when (unit) {
            WindSpeedUnit.MPS -> "m/s"
            WindSpeedUnit.KMPH -> "km/h"
            WindSpeedUnit.MPH -> "mph"
        }
        return "${speed.roundToInt()} $symbol"
    }
    fun convertFormatWindSpeed(speedInMps: Double, toUnit: WindSpeedUnit = SettingsPrefs.getWindSpeedUnit()): String {
        return formatWindSpeed(convertWindSpeed(speedInMps, toUnit), toUnit)
    }
    fun formatTemperature(value: Int, unit: TempUnit=SettingsPrefs.getTempUnit()): String {

        val symbol = when (unit) {
            TempUnit.CELSIUS -> "°C"
            TempUnit.FAHRENHEIT -> "°F"
            TempUnit.KELVIN -> "K"
        }
        return "$value$symbol"
    }
    fun applyLocale(context: Context, langCode: String) {


            val resources = context.resources
            val dm = resources.displayMetrics
            val config: Configuration = resources.configuration
            config.setLocale(Locale(langCode))
            context.resources.updateConfiguration(config, dm)
        }

    fun updateAppLocale(context: Context) {
        val langCode = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("language", "en") ?: "en"
        applyLocale(context, langCode)
    }
}