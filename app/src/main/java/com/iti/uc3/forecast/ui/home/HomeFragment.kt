package com.iti.uc3.forecast.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.iti.uc3.LocaleHelper
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import com.iti.uc3.forecast.databinding.WeatherScrollViewBinding
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.main.NavigationHost


class HomeFragment : Fragment() {


    private  lateinit var binding: WeatherScrollViewBinding



   // private lateinit  var viewModel: HomeViewModel
    private lateinit var navigationHost: NavigationHost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        LocaleHelper.applyLocale( requireContext(), SettingsPrefs.getLang())
        navigationHost = requireActivity() as NavigationHost


    }

    override fun onStart() {
        super.onStart()
        navigationHost.bottomNavigationView(R.id.nav_home)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationHost = context as NavigationHost
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val sunriseView = binding.sunRiseViewComponent  //= view.findViewById<SunriseSunsetView>(R.id.sunriseView)
//        // Initialize the ViewModel
//        viewModel.getWeatherByname(SettingsPrefs.getCurrentCity()    )
//        val chartView = binding.hourlyChart  //= view.findViewById<HourlyChartView>(R.id.hourlyChart)
//        val scrollView = binding.hourlyScroll //view.findViewById<HorizontalScrollView>(R.id.hourlyScroll)

        // Observe the weather data
//        viewModel.currentWeather.observe(viewLifecycleOwner) { weather ->
//
//
//            binding.tvTemp.text =convertformatTemperature( weather.list[0].main.temp.toInt())
//            binding.tvLocation.text = "${weather.city.name}, ${weather.city.country}"            // Example: "Cairo, EG"
//            binding.tvHighLow.text= "${convertformatTemperature(weather.list[0].main.temp_max.toInt())}" +
//                    " / ${convertformatTemperature(weather.list[0].main.temp_min.toInt())} "
//        //    binding.tvWind.text= "${weather.list[0].wind.speed} ${SettingsPrefs.getSpeedUnit()}"
//
//            val offset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//
//                ZoneOffset.ofTotalSeconds(weather.city.timezone)
//
//
//            } else {
//                TODO("VERSION.SDK_INT < O")
//            }
//
//            val zoneIds = ZoneId.getAvailableZoneIds()
//            for (id in zoneIds) {
//                val zoneId = ZoneId.of(id)
//                val rules = zoneId.getRules()
//                if (rules.getOffset(Instant.now()) == offset) {
//                    println("Matching Zone ID: " + id)
//                }
//            }
//
//            val sunriseTimestampSeconds = weather.city.sunrise // Example: March 25, 2023 06:58:00 GMT sunset
//            val sunsetTimestampSeconds =  weather.city.sunset  // Example: March 25, 2023 19:47:00 GMT
//            val timeZoneIdentifier = TimeZone.getDefault().id // Or a specific one like "America/New_York"
//
//            Log.d("HomeFragment", "Sunrise: $sunriseTimestampSeconds, Sunset: $sunsetTimestampSeconds, TimeZone: $timeZoneIdentifier")
//            // --- End Example Data ---
//
//            // Create the SunTimes data object
//            val sunTimesData = SunTimes(
//                sunriseEpoch = sunriseTimestampSeconds,
//                sunsetEpoch = sunsetTimestampSeconds,
//                timeZoneId = timeZoneIdentifier
//            )
//
//            // Update the view
//            sunriseView.setSunTimes(sunTimesData)
//
//            val groupedByDate: Map<String, List<ForecastItemEntity>> = weather.list
//                .groupBy { it.dt_txt.substringBefore(" ") }
//
//
//
//            val firstDayForecastList: List<ForecastItemEntity>? = groupedByDate.values.firstOrNull()
//
//            if(firstDayForecastList != null) {
//                val hourlyForecastList = firstDayForecastList.map {
//                    val hour = it.dt_txt.substringAfter(" ").substring(0, 5) // Extract HH:mm
//                    HourlyForecast(hour,convertTemperature( it.main.temp.toInt(), SettingsPrefs.getTempUnit()))
//                }
//                chartView.setData(hourlyForecastList,SettingsPrefs.getTempUnit())
//            }
//
//            // Update your UI with the weather data
//           // binding.weatherTextView.text = weather.toString() // Replace with actual UI update logic
//        }


//        val forecast = List(24) {
//            val hour = if (it < 10) "0$it:00" else "$it:00"
//            HourlyForecast(hour, (15..35).random())
//        }
//
//        chartView.setData(forecast)



//        scrollView.post {
//            scrollView.scrollTo(0, 0)
//        }



        // Replace these with your actual data source (e.g., weather API response)
        // Timestamps should be in SECONDS since epoch

        // Example data

      //  sunriseView.setSunTimes(1716700200, 1716740200)

    }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {

            binding= WeatherScrollViewBinding.inflate(inflater, container, false)
            return binding.root

        }
    }
