package com.iti.uc3.forecast.ui.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.ui.main.NavigationHost

class HomeFragment : Fragment() {


    private val viewModel: HomeViewModel by viewModels()
    private lateinit var navigationHost: NavigationHost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationHost = requireActivity() as NavigationHost
        // TODO: Use the ViewModel
    }
    override fun onStart() {
        super.onStart()
        navigationHost.bottomNavigationView(R.id.nav_home)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_home, container, false)
        navigationHost = requireActivity() as NavigationHost
    }
}