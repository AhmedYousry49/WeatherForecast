package com.iti.uc3.forecast.alerts


import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.WorkManager
import com.google.android.material.chip.Chip
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.databinding.FragmentWeatherAlertBinding
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.main.IMainView
import com.iti.uc3.forecast.ui.managecity.ManageCityFragment
import com.iti.uc3.forecast.ui.managecity.ManageCityViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fragment for setting up weather alerts with specific time and city
 */
class WeatherAlertFragment : Fragment() {

    private var _binding: FragmentWeatherAlertBinding? = null
    private val binding get() = _binding!!



    private var selectedHour = 7
    private var selectedMinute = 0
    private var selectedCityId = -1
    private var selectedCityName = ""
    private val selectedDays = mutableSetOf<Int>() // Calendar.MONDAY, Calendar.TUESDAY, etc.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val viewModel: ManageCityViewModel by viewModels {
        ViewModelFactoryProvider.factory {
            ManageCityViewModel(RepositoryProvider.getRepository(requireContext()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTimeSelection()
        setupDaySelection()
        setupCitySelection()
        setupSaveButton()
        // Observe cities list
        viewModel.currentWeather.observe(viewLifecycleOwner) { cities ->

            var hasCities = cities.map { it.city }
            setupCitySpinner(hasCities)
        }
        // Load saved cities
        viewModel.readAllCity()


    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupTimeSelection() {
        // Set initial time
        updateTimeDisplay()

        // Set up time picker dialog
        binding.selectedTimeText.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                updateTimeDisplay()
            },
            selectedHour,
            selectedMinute,
            false // 24-hour format
        )
        timePickerDialog.show()
    }

    private fun updateTimeDisplay() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.selectedTimeText.text = timeFormat.format(calendar.time)
    }

    private fun setupDaySelection() {
        // Set up day chips
        val dayChips = listOf(
            binding.mondayChip to Calendar.MONDAY,
            binding.tuesdayChip to Calendar.TUESDAY,
            binding.wednesdayChip to Calendar.WEDNESDAY,
            binding.thursdayChip to Calendar.THURSDAY,
            binding.fridayChip to Calendar.FRIDAY,
            binding.saturdayChip to Calendar.SATURDAY,
            binding.sundayChip to Calendar.SUNDAY
        )

        dayChips.forEach { (chip, day) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDays.add(day)
                } else {
                    selectedDays.remove(day)
                }
            }
        }
    }

    private fun setupCitySpinner(cities: List<CityEntity>) {
        val cityNames = cities.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cityNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.citySpinner.adapter = adapter
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < cities.size) {
                    selectedCityId = cities[position].id
                    selectedCityName = cities[position].name
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCityId = -1
                selectedCityName = ""
            }
        }
    }

    private fun setupCitySelection() {
        binding.addCityButton.setOnClickListener {
            (requireActivity() as? IMainView)?.navigateTo(
                ManageCityFragment(),
                true
            )
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                scheduleWeatherAlert()
            }
        }
    }

    private fun validateInput(): Boolean {
        if (selectedCityId == -1) {
            Toast.makeText(requireContext(), "Please select a city", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if at least one alert condition is selected
        val hasCondition = binding.rainCheckBox.isChecked ||
                binding.snowCheckBox.isChecked ||
                binding.extremeTemperatureCheckBox.isChecked ||
                binding.windCheckBox.isChecked

        if (!hasCondition) {
            Toast.makeText(requireContext(), "Please select at least one alert condition", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun scheduleWeatherAlert() {
        // Calculate time until alert
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        calendar.set(Calendar.SECOND, 0)

        // If the time is in the past, add a day
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = calendar.timeInMillis - now

        // Get alert conditions
        val alertRain = binding.rainCheckBox.isChecked
        val alertSnow = binding.snowCheckBox.isChecked
        val alertExtremeTemp = binding.extremeTemperatureCheckBox.isChecked
        val alertWind = binding.windCheckBox.isChecked

        // Schedule the alert
        if (selectedDays.isEmpty()) {
            // One-time alert
            WeatherAlertWorker.scheduleOneTimeAlert(
                requireContext(),
                selectedCityId,
                selectedCityName,
                initialDelay,
                alertRain,
                alertSnow,
                alertExtremeTemp,
                alertWind
            )

            Toast.makeText(
                requireContext(),
                "Weather alert scheduled for ${binding.selectedTimeText.text}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Recurring alert
            // For simplicity, we'll use a daily interval and let the worker check if it's the right day
            val repeatInterval = TimeUnit.DAYS.toMillis(1)

            WeatherAlertWorker.scheduleRecurringAlert(
                requireContext(),
                selectedCityId,
                selectedCityName,
                initialDelay,
                repeatInterval,
                alertRain,
                alertSnow,
                alertExtremeTemp,
                alertWind
            )

            val daysText = selectedDays.size.let {
                when {
                    it == 7 -> "every day"
                    it > 1 -> "on selected days"
                    else -> "on selected day"
                }
            }

            Toast.makeText(
                requireContext(),
                "Weather alert scheduled for ${binding.selectedTimeText.text} $daysText",
                Toast.LENGTH_LONG
            ).show()
        }

        // Navigate back
        requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
