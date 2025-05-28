package com.iti.uc3.forecast.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.iti.uc3.LocaleHelper
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import com.iti.uc3.forecast.databinding.ActivityMainBinding
import com.iti.uc3.forecast.fragments.HomeWeatherDetailsFragment
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.home.HomeFragment
import com.iti.uc3.forecast.ui.home.HomeViewModel
import com.iti.uc3.forecast.ui.managecity.ManageCityFragment
import com.iti.uc3.forecast.ui.searchcity.SearchCityFragment
import com.iti.uc3.forecast.ui.settings.SettingsFragment
import kotlinx.coroutines.launch
import kotlin.getValue

class MainActivity : AppCompatActivity()  , IMainView {


    private  lateinit  var binding: ActivityMainBinding
    private  lateinit var homeFragment: Fragment
    private  lateinit var settingsFragment: Fragment



    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocaleHelper.applyLocale( this, SettingsPrefs.getLang())

        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding.getRoot())

        binding.bottomNavigation.visibility=View.GONE
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

        val selectedItemId = savedInstanceState?.getInt("selected_item")
        selectedItemId?.let {
            binding.bottomNavigation.selectedItemId = it
        }
//        val repository = RepositoryProvider.getRepository(applicationContext)
//
//        lifecycleScope.launch {
//            repository.fetchForecastByCityName("London")
//            repository.fetchForecastByCityName("Cairo")
//        }



        if(SettingsPrefs.isFirstTime())
        {



             //   navigateTo(ManageCityFragment(), false, "SearchCityFragmentTag")

            navigateTo(SearchCityFragment(), false, "SearchCityFragmentTag")
        }
        else
        {
            homeFragment = HomeWeatherDetailsFragment()
            navigateTo(homeFragment, false)
        }


    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

       //     val fragment = supportFragmentManager.findFragmentByTag("SearchCityFragmentTag") as? SearchCityFragment
         //   fragment?.onLocationPermissionGranted()

        }
    }

    public override fun navigateTo(fragment: Fragment, addToBackstack: Boolean)= navigateTo(fragment,addToBackstack, null)



    public  fun navigateTo(fragment: Fragment, addToBackstack: Boolean, tag: String? = null) {
        val transaction =
            getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment,tag)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the current state of the bottom navigation view
        outState.putInt("selected_item", binding.bottomNavigation.selectedItemId)

    }
    override fun bottomNavigationView(i: Int) {
        binding.bottomNavigation.setSelectedItemId(i)

    }
}