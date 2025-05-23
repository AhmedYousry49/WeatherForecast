package com.iti.uc3.forecast

import android.app.Application
import com.iti.uc3.forecast.data.network.NetworkConnection
import com.iti.uc3.forecast.data.settings.SettingsPrefs

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsPrefs.init(this)
        NetworkConnection.init(this)


    }
}