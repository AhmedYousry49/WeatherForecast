package com.iti.uc3.forecast.ui.managecity;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.data.model.CityEntity
import com.iti.uc3.forecast.data.model.ForecastItemEntity
import com.iti.uc3.forecast.data.network.NetworkConnection
import com.iti.uc3.forecast.data.network.api.dto.WeatherForecastResponse
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import com.iti.uc3.forecast.databinding.FragmentManageCityBinding
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.WeatherDetailsFragment
import com.iti.uc3.forecast.ui.addcity.AddCity
import com.iti.uc3.forecast.ui.home.HomeFragment
import com.iti.uc3.forecast.ui.home.HomeViewModel
import com.iti.uc3.forecast.ui.main.IMainView
import kotlin.getValue


class ManageCityFragment : Fragment() {

    private var _binding: FragmentManageCityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ManageCityViewModel by viewModels {
        ViewModelFactoryProvider.factory {
            ManageCityViewModel(RepositoryProvider.getRepository(requireContext()))
        }
    }
    private lateinit var manageCityAdapter: ManageCityAdapter
    private var isSelectionModeActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageCityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        loadCities() // Load initial data



        // Observe ViewModel data if needed
        viewModel.currentWeather.observe(viewLifecycleOwner) { weatherList ->


            // Update the adapter with the new data
            manageCityAdapter.submitList( weatherList.mapNotNull { it.toManagedCity() })
        }
    }

    fun WeatherForecastResponse.toManagedCity(): ManagedCity? {
        val forecast = list.firstOrNull() ?: return null
        val weather = forecast.weather.firstOrNull()

        return ManagedCity(
            id = city.id, // or just city.id.toString()
            name = city.name,
            country = city.country,
            temperature = forecast.main.temp.toInt(),
            weatherCondition = weather?.main ?: "Unknown",
            backgroundImageResId = R.drawable.city_background_placeholder,
            weatherIconResId = mapWeatherToIcon(weather?.main),
            isCurrentLocation = SettingsPrefs.getCurrentCity()==city.name,
            hasNotification = false // Adjust as needed
        )
    }


    fun mapWeatherToIcon(condition: String?): Int {
        return when (condition?.lowercase()) {
            "clear" -> R.drawable.bg_citymanager_card_sunny
            "clouds" -> R.drawable.bg_citymanager_cloudy_0722
            "rain" -> R.drawable.bg_citymanager_card_rainy
            "snow" -> R.drawable.bg_citymanager_night_snow_0722
            else -> R.drawable.bg_citymanager_night_snow_0722
        }
    }

    private fun setupToolbar() {
        // Normal Toolbar actions
        binding.toolbarNormal.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed() // Handle back press
        }
        binding.addCityButton.setOnClickListener {
            // TODO: Navigate to Add City screen
         //   Toast.makeText(context, "Add City Clicked", Toast.LENGTH_SHORT).show()
            if(NetworkConnection.getCurrentState()) {
                (requireActivity() as? IMainView)?.navigateTo(
                    AddCity(),
                    true
                )
            }
            else
            {
                Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            }
            // Example: navigationHost.navigateToAddCity()
            // You can use a navigation component or start an activity
        }
        binding.reorderButton.isClickable=true
        binding.deleteButton.isClickable=true
        // Selection Toolbar actions
        binding.toolbarSelection.setNavigationOnClickListener {
            exitSelectionMode()
        }

        fun showDeleteConfirmationDialog() {
            val selectedIds = manageCityAdapter.getSelectedItems()
            val count = selectedIds.size
            if (count == 0) {
                Toast.makeText(context, "No items selected", Toast.LENGTH_SHORT).show()
                return
            }

            val message = if (count == 1) {
                "Are you sure you want to delete the selected city?"
            } else {
                "Are you sure you want to delete the $count selected cities?"
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage(message)
                .setPositiveButton("Delete") { dialog, _ ->
                    // TODO: Implement actual deletion logic here (e.g., call ViewModel)
                    // Pass selectedIds to the deletion function
                    Toast.makeText(context, "Deleting $count items...", Toast.LENGTH_SHORT).show()
                    // For now, just exit selection mode after confirmation
                    viewModel.deleteCities(selectedIds.toList())

                    exitSelectionMode()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        binding.deleteButton.setOnClickListener {
            val selectedIds = manageCityAdapter.getSelectedItems()
            if (selectedIds.isNotEmpty()) {
                showDeleteConfirmationDialog()
                exitSelectionMode()
            } else {
                Toast.makeText(context, "No items selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.reorderButton.setOnClickListener {
            // TODO: Implement reorder logic if needed, or use drag handle
         //   Toast.makeText(context, "Reorder Clicked", Toast.LENGTH_SHORT).show()

            manageCityAdapter.toggleSelectAll()
        }

        // Initially show normal toolbar
        updateToolbarVisibility()
    }

    private fun setupRecyclerView() {
        manageCityAdapter = ManageCityAdapter(
            onItemClicked = { city ->
                if (isSelectionModeActive) {
                    // Adapter handles selection toggle internally
                } else {
                    // TODO: Handle normal item click (e.g., navigate to city details)
                 //   Toast.makeText(context, "Clicked: ${city.name}", Toast.LENGTH_SHORT).show()

                    (requireActivity() as? IMainView)?.navigateTo(
                        WeatherDetailsFragment(city.name),
                        true
                    )
                }
            },
            onSelectionChanged = { selectedIds ->
                if (isSelectionModeActive) {
                    updateSelectionToolbar(selectedIds.size)
                }
            },
            onLongPress= {
                enterSelectionMode()

            }
        )

        binding.manageCityRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = manageCityAdapter
        }

        // Optional: Add long press to enter selection mode
        // You might need a custom implementation or library for this
        // Example: Add a long click listener in the ViewHolder or use a library

        // Optional: Add ItemTouchHelper for drag-and-drop reordering (requires more setup)
        // val itemTouchHelper = ItemTouchHelper(DragManageCallback(manageCityAdapter))
        // itemTouchHelper.attachToRecyclerView(binding.manageCityRecyclerView)
    }

    private fun loadCities() {
        viewModel.readAllCity()
//        val dummyCities = listOf(
//            ManagedCity("1", "Kirdasa", "Egypt", 30, "Cloudy", R.drawable.city_background_placeholder, R.drawable.bg_citymanager_cloudy_0722, true, true),
//            ManagedCity("2", "New York", "United States", 16, "Sunny", R.drawable.city_background_placeholder, R.drawable.bg_citymanager_card_sunny),
//            ManagedCity("3", "Sandu County", "China", 22, "Cloudy", R.drawable.city_background_placeholder, R.drawable.bg_citymanager_cloudy_0722),
//            ManagedCity("4", "Paris", "France", 18, "Cloudy", R.drawable.city_background_placeholder, R.drawable.bg_citymanager_card_night_cloudy),
//            ManagedCity("5", "Tacuarendí", "Argentina", 20, "Cloudy", R.drawable.city_background_placeholder, R.drawable.bg_citymanager_cloudy_0722),
//            ManagedCity("6", "Alegrete", "Portugal", 27, "Sunny", R.drawable.city_background_placeholder, R.drawable.bg_citymanager_night_snow_0722)
//            // Add more dummy data
//        )
//        manageCityAdapter.submitList(dummyCities)
    }

    // Call this function to enter selection mode (e.g., on long press)
    private fun enterSelectionMode() {
        if (isSelectionModeActive) return
        isSelectionModeActive = true
        manageCityAdapter.toggleSelectionMode(true)
        updateToolbarVisibility()
        updateSelectionToolbar(0) // Start with 0 selected
    }

    private fun exitSelectionMode() {
        if (!isSelectionModeActive) return
        isSelectionModeActive = false
        manageCityAdapter.toggleSelectionMode(false)
        updateToolbarVisibility()
    }

    private fun updateToolbarVisibility() {
        if (isSelectionModeActive) {
            binding.toolbarNormal.visibility = View.GONE
            binding.toolbarSelection.visibility = View.VISIBLE
        } else {
            binding.toolbarNormal.visibility = View.VISIBLE
            binding.toolbarSelection.visibility = View.GONE
        }
    }

    private fun updateSelectionToolbar(count: Int) {
        binding.selectionCountText.text = count.toString()
        // Enable/disable buttons based on count if needed
//        binding.deleteButton.isEnabled = count > 0
//        binding.reorderButton.isEnabled = count > 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    // Optional: Handle back press to exit selection mode first
    // You might need to coordinate this with the hosting Activity
    fun handleBackPressed(): Boolean {
        if (isSelectionModeActive) {
            exitSelectionMode()
            return true // Indicate back press was handled
        }
        return false // Allow normal back press behavior
    }

    // TODO: Implement ItemTouchHelper.Callback if drag-and-drop is needed
    // class DragManageCallback(private val adapter: ManageCityAdapter) : ItemTouchHelper.Callback() { ... }
}

