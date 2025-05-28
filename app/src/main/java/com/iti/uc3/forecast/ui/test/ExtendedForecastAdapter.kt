package com.iti.uc3.forecast.ui.test
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.uc3.forecast.R // Assuming R is in this package
import com.iti.uc3.forecast.data.model.DailyTemperatureRange
import com.iti.uc3.forecast.data.model.ForecastItemEntity
import com.iti.uc3.forecast.databinding.ItemExtendedForecastBinding // Assuming ViewBinding is setup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ExtendedForecastAdapter(private val context: Context) : // Pass context if needed for resources/formatters
    ListAdapter<DailyTemperatureRange, ExtendedForecastAdapter.ForecastViewHolder>(ForecastDiffCallback()) {

    // Date formatters (consider making these reusable or injecting them)
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Format for day name (e.g., "Wed")
    private val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Assuming dt_txt is UTC
        // Adjust timezones as needed based on your API/data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemExtendedForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding, context, inputFormat, dayFormat, todayFormat)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecastItem = getItem(position)
        holder.bind(forecastItem)
    }

    class ForecastViewHolder(
        private val binding: ItemExtendedForecastBinding,
        private val context: Context,
        private val inputFormat: SimpleDateFormat,
        private val dayFormat: SimpleDateFormat,
        private val todayFormat: SimpleDateFormat
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyTemperatureRange) {

            binding.dayNameText.text = item.dayName
            binding.weatherDescriptionText.text = item?.description?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "N/A"
            binding.temperatureRangeText.text = "${item.tempMin}/${item.tempMax}"

            // Set weather icon based on icon code
         //   val iconResId = getWeatherIconRes(weatherCondition?.icon)
         //   binding.weatherIcon.setImageResource(iconResId)
        }

        private fun getDayName(dtTxt: String): String {
            return try {
                val date = inputFormat.parse(dtTxt)
                val todayStr = todayFormat.format(Date()) // Today's date
                val itemDateStr = todayFormat.format(date)

                if (todayStr == itemDateStr) {
                    "Today"
                } else {
                    dayFormat.format(date)
                }
            } catch (e: Exception) {
                "N/A" // Handle parsing error
            }
        }

        // Helper function to map weather icon codes to drawable resources
//        private fun getWeatherIconRes(iconCode: String?): Int {
//            // TODO: Implement mapping based on OpenWeatherMap icon codes
//            // Example mapping (add all necessary codes):
////            return when (iconCode) {
////                "01d" -> R.drawable.ic_weather_sunny_placeholder // Replace with actual drawables
////                "01n" -> R.drawable.ic_weather_clear_night_placeholder
////                "02d" -> R.drawable.ic_weather_partly_cloudy_day_placeholder
////                "02n" -> R.drawable.ic_weather_partly_cloudy_night_placeholder
////                "03d", "03n" -> R.drawable.ic_weather_cloudy_placeholder // Scattered clouds
////                "04d", "04n" -> R.drawable.ic_weather_overcast_placeholder // Broken clouds
////                "09d", "09n" -> R.drawable.ic_weather_showers_placeholder // Shower rain
////                "10d" -> R.drawable.ic_weather_rain_day_placeholder
////                "10n" -> R.drawable.ic_weather_rain_night_placeholder
////                "11d", "11n" -> R.drawable.ic_weather_thunderstorm_placeholder
////                "13d", "13n" -> R.drawable.ic_weather_snow_placeholder
////                "50d", "50n" -> R.drawable.ic_weather_mist_placeholder
////                else -> R.drawable.ic_weather_unknown_placeholder // Default/unknown icon
////            }
//        }
    }

    class ForecastDiffCallback : DiffUtil.ItemCallback<DailyTemperatureRange>() {
        override fun areItemsTheSame(oldItem: DailyTemperatureRange, newItem: DailyTemperatureRange): Boolean {
            // Use dt (timestamp) and cityId for uniqueness
            return oldItem.dayName == newItem.dayName
        }

        override fun areContentsTheSame(oldItem: DailyTemperatureRange, newItem: DailyTemperatureRange): Boolean {
            // Check if the content is the same
            return oldItem == newItem
        }
    }
}

