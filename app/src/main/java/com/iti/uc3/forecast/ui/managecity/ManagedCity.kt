package com.iti.uc3.forecast.ui.managecity;

data class ManagedCity(
        var id: String, // Unique ID for selection/diffing
        val name: String,
        val country: String,
        val temperature: Int,
        val weatherCondition: String, // e.g., "Sunny", "Cloudy"
        val backgroundImageResId: Int, // Placeholder for background
        val weatherIconResId: Int, // Placeholder for icon
        val isCurrentLocation: Boolean = false,
        val hasNotification: Boolean = false
)