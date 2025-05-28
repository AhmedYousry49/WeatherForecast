package com.iti.uc3.forecast.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object ViewModelFactoryProvider {

    // Generic Factory
    class Factory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return creator() as T
        }
    }

    // Generic helper to use in Fragments
    inline fun <reified T : ViewModel> from(fragment: Fragment, crossinline creator: () -> T): T {
        val factory = Factory { creator() }
        return ViewModelProvider(fragment, factory)[T::class.java]
    }

    // Optional: helper for Activities
    inline fun <reified T : ViewModel> from(activity: AppCompatActivity, crossinline creator: () -> T): T {
        val factory = Factory { creator() }
        return ViewModelProvider(activity, factory)[T::class.java]
    }

    inline fun <T : ViewModel> factory(crossinline creator: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return creator() as T
            }
        }
    }
}

