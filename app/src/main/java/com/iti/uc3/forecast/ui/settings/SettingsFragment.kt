package com.iti.uc3.forecast.ui.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.iti.uc3.LocaleHelper
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.ui.main.NavigationHost

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var navigationHost: NavigationHost
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val langPref = findPreference<ListPreference>("language")
        langPref?.setOnPreferenceChangeListener { _, newValue ->

            val newLang = newValue.toString()
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("language", newLang).apply()
            // Apply locale and recreate activity
            LocaleHelper.applyLocale(requireContext().applicationContext, newLang)
            activity?.recreate()
            true
        }
        navigationHost = requireActivity() as NavigationHost
    }

    override fun onStart() {
        super.onStart()
        navigationHost.bottomNavigationView(R.id.nav_settings)
    }


}