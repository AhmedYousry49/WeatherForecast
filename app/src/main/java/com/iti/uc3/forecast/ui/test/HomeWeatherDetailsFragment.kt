package com.iti.uc3.forecast.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.uc3.LocaleHelper
import com.iti.uc3.LocaleHelper.convertTemperature
import com.iti.uc3.LocaleHelper.convertformatTemperature
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.alerts.WeatherAlertFragment
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.DailyTemperatureRange
import com.iti.uc3.forecast.data.model.ForecastItemEntity
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import com.iti.uc3.forecast.databinding.FragmentWeatherDetailsBinding
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.home.HomeViewModel
import com.iti.uc3.forecast.ui.main.IMainView
import com.iti.uc3.forecast.ui.managecity.ManageCityFragment
import com.iti.uc3.forecast.ui.settings.SettingsFragment
import com.iti.uc3.forecast.ui.test.ExtendedForecastAdapter
import com.iti.uc3.forecast.view.HourlyForecast
import com.iti.uc3.forecast.view.SunTimes
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.getValue
import kotlin.math.roundToInt

class HomeWeatherDetailsFragment(val CityName: String=SettingsPrefs.getCurrentCity() ,val Showapp: Boolean=true) : Fragment() {

    private var _binding: FragmentWeatherDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactoryProvider.factory {
            HomeViewModel(RepositoryProvider.getRepository(requireContext()))
        }
    }
    private lateinit var extendedForecastAdapter: ExtendedForecastAdapter

    // Interface for refresh callback
    interface WeatherRefreshListener {
        fun onRefreshWeather()
    }

    private var refreshListener: WeatherRefreshListener? = null

    fun setWeatherRefreshListener(listener: WeatherRefreshListener) {
        this.refreshListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        viewModel.getWeatherByname(CityName)

        viewModel.currentWeather.observe(viewLifecycleOwner) { weather ->
            binding.swipeRefreshLayout.isRefreshing = false
            weather?.let {
                displayWeatherData(it)
            } ?: run {
                Toast.makeText(context, "Error loading weather data", Toast.LENGTH_LONG).show()
            }
        }
        // TODO: Replace this with actual data loading mechanism
        // e.g., observe from ViewModel, receive via arguments
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Trigger refresh
            refreshListener?.onRefreshWeather() ?: run {
                // If no listener is set, implement default refresh behavior
                // For example, you could call a method in your ViewModel
                // viewModel.refreshWeatherData()
                viewModel.getWeatherByname(SettingsPrefs.getCurrentCity()    )
                // For demo purposes, just hide the refresh indicator after a delay
//                binding.swipeRefreshLayout.postDelayed({
//                    binding.swipeRefreshLayout.isRefreshing = false
//                    Toast.makeText(context, "Implement actual refresh logic", Toast.LENGTH_SHORT).show()
//                }, 1500)
            }
        }

        // Customize SwipeRefreshLayout colors if needed
//        binding.swipeRefreshLayout.setColorSchemeResources(
//            R.color.colorPrimary,
//            R.color.colorAccent,
//            R.color.colorPrimaryDark
//        )
    }

    // Call this method when you have the WeatherForecastResponse data
    @RequiresApi(Build.VERSION_CODES.O)
    fun displayWeatherData(response: WeatherForecastResponse) {
        val city = response.city
        val forecastList = response.list
        val currentForecast = forecastList.firstOrNull() // Use the first item as "current"

        if (currentForecast != null) {


            val groupedByDate: Map<String, List<ForecastItemEntity>> = forecastList
                .groupBy { it.dt_txt.substringBefore(" ") }



            val firstDayForecastList: List<ForecastItemEntity>? = groupedByDate.values.firstOrNull()



            val now = LocalDateTime.now()

            val closestForecast = firstDayForecastList!!
                .map { forecast ->
                    val time = LocalDateTime.ofInstant(Instant.ofEpochSecond(forecast.dt), ZoneId.systemDefault())
                    forecast to time
                }
                .filter { (_, time) ->
                    time.isAfter(now)
                }
                .minByOrNull { (_, time) ->
                    java.time.Duration.between(now, time).toMinutes()
                }
                ?.first // get the forecast object

            bindCurrentWeather(city, closestForecast!!)
            if(firstDayForecastList != null) {
                val hourlyForecastList = firstDayForecastList.flatMap {
                    val hourText = it.dt_txt.substringAfter(" ").substring(0, 5) // "HH:mm"
                    val baseHour = hourText.substringBefore(":").toIntOrNull() ?: 0
                    val temp = convertTemperature(it.main.temp.toInt(), SettingsPrefs.getTempUnit())

                    // Create 3 HourlyForecasts at hour, hour+1, hour+2
                    List(3) { offset ->
                        val hour = (baseHour + offset).coerceIn(0, 23).toString().padStart(2, '0')
                        HourlyForecast("$hour:00", temp)
                    }
                }

                val dailyRanges = forecastList
                    .groupBy { it.dt_txt.substringBefore(" ") }
                    .map { (date, itemsForDate) ->
                        val min = itemsForDate.minOf { it.main.temp_min }
                        val max = itemsForDate.maxOf { it.main.temp_max }

                        val representativeItem = itemsForDate.find { it.dt_txt.contains("12:00:00") }
                            ?: itemsForDate.first()

                        val weather = representativeItem.weather.firstOrNull()
                        val description = weather?.description?.replaceFirstChar(Char::uppercaseChar) ?: "N/A"
                        val icon = weather?.icon ?: ""

                        val dayName = try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val dateObj = sdf.parse(date)
                            SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj ?: Date())
                        } catch (e: Exception) {
                            "Unknown"
                        }

                        DailyTemperatureRange(
                            dayName = dayName,
                            date = date,
                            tempMin = convertformatTemperature(min.toInt()),
                            tempMax = convertformatTemperature(max.toInt()),
                            description = description,
                            icon = icon
                        )
                    }

                val sunriseTimestampSeconds = city.sunrise // Example: March 25, 2023 06:58:00 GMT sunset
            val sunsetTimestampSeconds =  city.sunset  // Example: March 25, 2023 19:47:00 GMT
            val timeZoneIdentifier = TimeZone.getDefault().id // Or a specific one like "America/New_York"

            Log.d("HomeFragment", "Sunrise: $sunriseTimestampSeconds, Sunset: $sunsetTimestampSeconds, TimeZone: $timeZoneIdentifier")
            // --- End Example Data ---

            // Create the SunTimes data object
            val sunTimesData = SunTimes(
                sunriseEpoch = sunriseTimestampSeconds,
                sunsetEpoch = sunsetTimestampSeconds,
                timeZoneId = timeZoneIdentifier
            )

                binding.sunRiseViewComponent.setSunTimes(sunTimesData)

                binding.airQualityValue.text= closestForecast.main.sea_level.toString()
                binding.uvIndexValue.text= closestForecast.main.pressure.toString()
               binding.feelsLikeValue.text= convertformatTemperature(closestForecast.main.feels_like.toInt())

                //binding.hourlyChart.
                binding.hourlyChart.setData(hourlyForecastList,SettingsPrefs.getTempUnit())
                extendedForecastAdapter.submitList(dailyRanges.drop(1))
            }


            // Filter the list for the adapter if needed (e.g., only show one item per day)

        } else {
            // Handle error case where forecast list is empty
            Toast.makeText(context, "Error: No forecast data available", Toast.LENGTH_LONG).show()
        }

        // Stop the refresh animation if it's running
        binding.swipeRefreshLayout.isRefreshing = false

        // TODO: Load data and configure the hourly forecast custom view
        // binding.hourlyForecastGraphPlaceholder.setData(forecastList)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // TODO: Handle navigation icon click
            Toast.makeText(context, "Navigation Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view)
        }
    }

    private fun showOptionsMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.bottom_nav, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    (requireActivity() as? IMainView)?.navigateTo(
                        SettingsFragment(),
                        true
                    )
                    true
                }
                R.id.nav_alert -> {
                    (requireActivity() as? IMainView)?.navigateTo(
                        WeatherAlertFragment(),
                        true
                    )
                    true
                }



                R.id.action_refresh -> {
                    // Show refresh indicator
                    binding.swipeRefreshLayout.isRefreshing = true
                    // Trigger refresh
                    refreshListener?.onRefreshWeather() ?: run {
                        // Hide indicator after delay if no listener
                        binding.swipeRefreshLayout.postDelayed({
                            binding.swipeRefreshLayout.isRefreshing = false
                        }, 1500)
                    }
                    true
                }
                R.id.nav_favourite -> {
                    (requireActivity() as? IMainView)?.navigateTo(
                        ManageCityFragment(),
                        true
                    )
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupRecyclerView() {
        // Pass context to adapter if needed (e.g., for string resources)
        extendedForecastAdapter = ExtendedForecastAdapter(requireContext())
        binding.extendedForecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = extendedForecastAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun bindCurrentWeather(city: CityEntity, currentForecast: ForecastItemEntity) {
        val currentWeatherCondition = currentForecast.weather.firstOrNull()

        binding.toolbar.title = city.name
        binding.currentTemperatureText.text = LocaleHelper.convertformatTemperature( currentForecast.main.temp.roundToInt())// Assuming temp is in Kelvin, convert if needed
        binding.tempRangeText.text = "${ LocaleHelper.convertTemperature(currentForecast.main.temp_max.roundToInt())}/${LocaleHelper.convertformatTemperature(currentForecast.main.temp_min.roundToInt())}"
        binding.currentConditionText.text = currentWeatherCondition?.description?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "N/A"
        binding.windSpeedText.text = LocaleHelper.convertFormatWindSpeed(currentForecast.wind.speed)  // Assuming m/s, convert if needed
        binding.humidityText.text = "${currentForecast.main.humidity}%"
        // Rain data might not be directly available in ForecastItemEntity, check API response structure
        binding.rainText.visibility = View.GONE // Hide rain text for now
        binding.rainIcon.visibility = View.GONE

        // TODO: Implement logic to get appropriate background based on weather/time
        binding.weatherBackgroundImageView.setImageResource(R.drawable.bg_sunny_v2)





        // TODO: Check for actual weather warnings if available in your data source
        binding.weatherWarningText.visibility = View.GONE

        // TODO: Implement logic for the curved background view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
