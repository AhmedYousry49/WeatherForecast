package com.iti.uc3.forecast.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.ui.main.NavigationHost

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var navigationHost: NavigationHost
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        navigationHost = requireActivity() as NavigationHost
    }

    override fun onStart() {
        super.onStart()
        navigationHost.bottomNavigationView(R.id.nav_settings)
    }
}