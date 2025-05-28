package com.iti.uc3.forecast

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.iti.uc3.LocaleHelper
import com.iti.uc3.forecast.data.network.NetworkConnection
import com.iti.uc3.forecast.data.settings.SettingsPrefs

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsPrefs.init(this)
        NetworkConnection.init(this)
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        )

    }
    override fun attachBaseContext(base: Context) {
      LocaleHelper.applyLocale(base, getSavedLang(base))
        super.attachBaseContext(base)
    }

    private fun getSavedLang(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("language", "en") ?: "en"
    }
}