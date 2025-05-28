package com.iti.uc3.forecast.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.databinding.ActivitySplashBinding
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.home.HomeViewModel
import com.iti.uc3.forecast.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue


class SplashActivity  : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactoryProvider.factory {
            HomeViewModel(RepositoryProvider.getRepository(this))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        // Set the content view to the binding root
        setContentView(binding.root)

        lifecycleScope.launch {

            viewModel.UpdateDatebase() // Update the database with the latest weather data
            // Simulate a delay for splash screen
            delay(1000)

            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // Finish the splash activity so that it is removed from the back stack
            finish()

        }

    }
}