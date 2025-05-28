package com.iti.uc3.forecast.alerts


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker class responsible for checking weather conditions and showing alerts
 * based on user-defined criteria.
 */
class WeatherAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME_PREFIX = "weather_alert_"
        const val KEY_CITY_ID = "city_id"
        const val KEY_CITY_NAME = "city_name"
        const val KEY_ALERT_RAIN = "alert_rain"
        const val KEY_ALERT_SNOW = "alert_snow"
        const val KEY_ALERT_EXTREME_TEMP = "alert_extreme_temp"
        const val KEY_ALERT_WIND = "alert_wind"

        private const val CHANNEL_ID = "weather_alerts"
        private const val NOTIFICATION_ID = 1000

        /**
         * Schedule a one-time weather alert check
         */
        fun scheduleOneTimeAlert(
            context: Context,
            cityId: Int,
            cityName: String,
            delayInMillis: Long,
            alertRain: Boolean = false,
            alertSnow: Boolean = false,
            alertExtremeTemp: Boolean = false,
            alertWind: Boolean = false
        ): Operation {
            val data = workDataOf(
                KEY_CITY_ID to cityId,
                KEY_CITY_NAME to cityName,
                KEY_ALERT_RAIN to alertRain,
                KEY_ALERT_SNOW to alertSnow,
                KEY_ALERT_EXTREME_TEMP to alertExtremeTemp,
                KEY_ALERT_WIND to alertWind
            )

            val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
                .setInputData(data)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .build()

            return WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "$WORK_NAME_PREFIX$cityId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }

        /**
         * Schedule a recurring weather alert check
         */
        fun scheduleRecurringAlert(
            context: Context,
            cityId: Int,
            cityName: String,
            initialDelayMillis: Long,
            repeatIntervalMillis: Long,
            alertRain: Boolean = false,
            alertSnow: Boolean = false,
            alertExtremeTemp: Boolean = false,
            alertWind: Boolean = false
        ): Operation {
            val data = workDataOf(
                KEY_CITY_ID to cityId,
                KEY_CITY_NAME to cityName,
                KEY_ALERT_RAIN to alertRain,
                KEY_ALERT_SNOW to alertSnow,
                KEY_ALERT_EXTREME_TEMP to alertExtremeTemp,
                KEY_ALERT_WIND to alertWind
            )

            val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
                repeatIntervalMillis, TimeUnit.MILLISECONDS
            )
                .setInputData(data)
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .build()

            return WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "$WORK_NAME_PREFIX$cityId",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
        }

        /**
         * Cancel a scheduled alert for a specific city
         */
        fun cancelAlert(context: Context, cityId: Int) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("$WORK_NAME_PREFIX$cityId")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get input data
            val cityId = inputData.getInt(KEY_CITY_ID, -1)
            val cityName = inputData.getString(KEY_CITY_NAME) ?: "Unknown"
            val alertRain = inputData.getBoolean(KEY_ALERT_RAIN, false)
            val alertSnow = inputData.getBoolean(KEY_ALERT_SNOW, false)
            val alertExtremeTemp = inputData.getBoolean(KEY_ALERT_EXTREME_TEMP, false)
            val alertWind = inputData.getBoolean(KEY_ALERT_WIND, false)

            if (cityId == -1) {
                return@withContext Result.failure()
            }

            // Get repository instance (you would need to implement this)
            val repository = RepositoryProvider.getRepository(applicationContext)// WeatherRepository.getInstance(applicationContext)

            // Fetch current weather for the city
            val weatherResponse = repository.getForecastByCityid(cityId)

            // Check if any alert conditions are met
            val shouldAlert = checkAlertConditions(
                weatherResponse,
                alertRain,
                alertSnow,
                alertExtremeTemp,
                alertWind
            )

            if (shouldAlert) {
                // Show notification
                showWeatherAlert(cityName, getAlertMessage(weatherResponse))
            }

            Result.success()
        } catch (e: Exception) {
            // Log error
            Result.failure()
        }
    }

    private fun checkAlertConditions(
        weatherResponse: WeatherForecastResponse?, // Replace with your actual weather response type
        alertRain: Boolean,
        alertSnow: Boolean,
        alertExtremeTemp: Boolean,
        alertWind: Boolean
    ): Boolean {
        // This is a placeholder implementation
        // You would need to implement the actual logic based on your weather data structure

        // Example implementation:
        /*
        if (alertRain && weatherResponse.hasRain()) {
            return true
        }

        if (alertSnow && weatherResponse.hasSnow()) {
            return true
        }

        if (alertExtremeTemp && weatherResponse.hasExtremeTemperature()) {
            return true
        }

        if (alertWind && weatherResponse.hasStrongWind()) {
            return true
        }
        */

        // For demonstration, always return true
        return true
    }

    private fun getAlertMessage(weatherResponse: WeatherForecastResponse?): String {
        // This is a placeholder implementation
        // You would need to implement the actual logic based on your weather data structure

        return "Current weather conditions require your attention."
    }

    private fun showWeatherAlert(cityName: String, message: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for weather alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.warning_24px)
            .setContentTitle("Weather Alert for $cityName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
