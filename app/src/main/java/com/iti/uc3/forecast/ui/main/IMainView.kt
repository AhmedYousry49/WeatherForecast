package com.iti.uc3.forecast.ui.main

import androidx.fragment.app.Fragment


interface NavigationHost {
    fun navigateTo(fragment: Fragment, addToBackstack: Boolean)
    fun bottomNavigationView(i: Int)
}

interface IMainView :  NavigationHost