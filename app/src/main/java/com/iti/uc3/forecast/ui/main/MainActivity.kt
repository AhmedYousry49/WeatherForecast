package com.iti.uc3.forecast.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.databinding.ActivityMainBinding
import com.iti.uc3.forecast.ui.home.HomeFragment
import com.iti.uc3.forecast.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity()  , IMainView {


    private  lateinit  var binding: ActivityMainBinding
    private  lateinit var homeFragment: Fragment
    private  lateinit var settingsFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding.getRoot())


        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if  (!::homeFragment.isInitialized) {
                        homeFragment = HomeFragment()
                    }
                    navigateTo(homeFragment, true)
                }

                R.id.nav_settings -> {
                    if  (!::settingsFragment.isInitialized) {
                        settingsFragment = SettingsFragment()
                    }
                    navigateTo(settingsFragment, true)
                }
            }
            true
        }
        homeFragment = HomeFragment()
        navigateTo(homeFragment, false)
    }

    public override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        val transaction =
            getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    override fun bottomNavigationView(i: Int) {
        binding.bottomNavigation.setSelectedItemId(i)

    }
}