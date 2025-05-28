package com.iti.uc3.forecast.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NetworkConnection {

    private lateinit var connectivityManager: ConnectivityManager
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> get() = _connectionState

    fun init(context: Context) {
        connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _connectionState.value = true
                Log.d("NetworkConnection", "Network available")
            }

            override fun onLost(network: Network) {
                _connectionState.value = false
                Log.d("NetworkConnection", "Network lost")
            }
        })

        _connectionState.value = getCurrentState()
    }

    fun getCurrentState(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}