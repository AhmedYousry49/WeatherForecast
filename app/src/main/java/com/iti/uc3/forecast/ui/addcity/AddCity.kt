package com.iti.uc3.forecast.ui.addcity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.iti.uc3.forecast.R
import com.iti.uc3.forecast.data.repository.RepositoryProvider
import com.iti.uc3.forecast.data.settings.SettingsPrefs
import com.iti.uc3.forecast.databinding.DialogRoundedBackgroundBinding
import com.iti.uc3.forecast.databinding.FragmentSearchCityBinding
import com.iti.uc3.forecast.ui.ViewModelFactoryProvider
import com.iti.uc3.forecast.ui.home.HomeFragment
import com.iti.uc3.forecast.ui.main.IMainView
import com.iti.uc3.forecast.ui.searchcity.CitySearchAdapter
import com.iti.uc3.forecast.ui.searchcity.SearchViewModel
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
 class AddCity : Fragment() {

        private var _binding: FragmentSearchCityBinding? = null
        private val binding get() = _binding!!

        private lateinit var citySearchAdapter: CitySearchAdapter
        private var locatingDialog: AlertDialog? = null
        private var notificationDialog: AlertDialog? = null

        private val viewModel: SearchViewModel by viewModels {
            ViewModelFactoryProvider.factory {
                SearchViewModel(RepositoryProvider.getRepository(requireContext()))
            }

        }


        // --- Location Permission Handling ---
        private val requestLocationPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    startLocationDetection()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    startLocationDetection() // Or handle coarse location differently
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // --- End Location Permission Handling ---

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {

            _binding = FragmentSearchCityBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
           // binding.detectLocationButton.visibility  = View.GONE

            setupToolbar()
            setupRecyclerView()
            setupSearch()
            setupDetectLocationButton()
            viewModel.fetchStatus.observe(viewLifecycleOwner) { isSuccess ->
                dismissLocatingDialog()
                if (isSuccess == true) {
                    Toast.makeText(context, "Location detected successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()


                } else {
                    Toast.makeText(context, "Failed to detect location", Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.currentCityName.observe(viewLifecycleOwner) { city ->

                if (city != null) {
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                    binding.detectLocationButton.visibility = View.GONE
                    citySearchAdapter.submitList(city)
                } else {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    binding.detectLocationButton.visibility = View.VISIBLE
                }
            }

            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.detectLocationButton.visibility = View.GONE

            binding.detectLocationButton.visibility = View.GONE

        }

        private fun setupToolbar() {
            binding.toolbar.setNavigationOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }
        }

        private fun setupRecyclerView() {
            citySearchAdapter = CitySearchAdapter { city ->


                showLocatingDialogIfNeeded()
                viewModel.fetchCityByName(city.name,false)
//            Toast.makeText(context, "Selected: ${city.name}", Toast.LENGTH_SHORT).show()
                // Handle selection
            }
            binding.searchResultsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = citySearchAdapter
            }
        }

        private fun setupSearch() {
            binding.searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s?.toString()?.trim() ?: ""
                    if (query.isNotEmpty()) {
                        binding.clearSearchIcon.visibility = View.VISIBLE
                  //      binding.detectLocationButton.visibility = View.GONE
                        binding.searchResultsRecyclerView.visibility = View.VISIBLE
                        performSearch(query)
                    } else {
                        binding.clearSearchIcon.visibility = View.GONE
                       // binding.detectLocationButton.visibility = View.VISIBLE
                        binding.searchResultsRecyclerView.visibility = View.GONE
                        citySearchAdapter.submitList(emptyList())
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            binding.clearSearchIcon.setOnClickListener { binding.searchEditText.text.clear() }
            binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(binding.searchEditText.text.toString().trim())
                    true
                } else false
            }
        }

        private fun setupDetectLocationButton() {
            binding.detectLocationButton.setOnClickListener {
                val notificationAgreed = SettingsPrefs.isnotificationAgreed()
                if (!notificationAgreed) {
                    showNotificationAgreementDialog()
                } else {
                    checkLocationPermissionAndProceed()
                }
            }
        }


        private fun checkLocationPermissionAndProceed() {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    startLocationDetection()


                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // Explain why you need the permission (e.g., in a separate dialog)
                    // Then launch the permission request
                    Toast.makeText(context, "Location permission is needed to detect your city automatically.", Toast.LENGTH_LONG).show()
                    requestLocationPermissions()
                }
                else -> {
                    // Directly request the permission
                    requestLocationPermissions()
                }
            }
        }

        private fun requestLocationPermissions() {
            requestLocationPermissionsLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
        @SuppressLint("MissingPermission") // You already check permission before calling this
        private fun startLocationDetection() {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        showLocatingDialogIfNeeded()
                        viewModel.fetchCityByCoordinates(latitude, longitude)
                        Log.d("LocationDetection", "Latitude: $latitude, Longitude: $longitude")


                        // You can proceed to fetch weather using these coordinates
                    } else {
                        Log.w("LocationDetection", "Location is null")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationDetection", "Failed to get location", e)
                }


        }


        private fun showLocatingDialogIfNeeded() {

            showLocatingDialog()




            // TODO: Implement actual location fetching logic (e.g., using FusedLocationProviderClient)
            // Simulate delay for demonstration
            /*      Handler(Looper.getMainLooper()).postDelayed({
                      dismissLocatingDialog()
                      val detectedCity = "Detected City Name" // Replace with actual result
                      Toast.makeText(context, "Location detected: $detectedCity", Toast.LENGTH_SHORT).show()
                      // Handle the detected city (e.g., navigate back with the result)
                  }, 3000) // Simulate 3 seconds delay*/
        }

        // Show a dialog while locating the user


        private fun showLocatingDialog() {
            if (locatingDialog == null) {
                val builder = AlertDialog.Builder(requireContext())
                val inflater = requireActivity().layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_locating, null) // Use your XML layout
                builder.setView(dialogView)
                builder.setCancelable(false) // Prevent dismissing while loading
                locatingDialog = builder.create()
                // Optional: Make dialog background transparent if your XML has its own background
                locatingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
            locatingDialog?.show()
        }

        private fun dismissLocatingDialog() {
            locatingDialog?.dismiss()
        }

        private fun showNotificationAgreementDialog() {
            if (notificationDialog == null) {
                val dialogBinding = DialogRoundedBackgroundBinding.inflate(LayoutInflater.from(context))
                val builder = AlertDialog.Builder(requireContext())
                builder.setView(dialogBinding.root)
                builder.setCancelable(false) // Or true if you want it cancelable
                notificationDialog = builder.create()
                notificationDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

                dialogBinding.agreeButton.setOnClickListener {
                    // User agreed - save preference and proceed
                    SettingsPrefs.setnotificationAgreed(true)

                    notificationDialog?.dismiss()
                    checkLocationPermissionAndProceed() // Proceed to location check
                }

                dialogBinding.disagreeButton.setOnClickListener {
                    // User disagreed - handle accordingly (e.g., disable location features)
                    Toast.makeText(context, "Location features require agreement.", Toast.LENGTH_SHORT).show()
                    notificationDialog?.dismiss()
                }

                // TODO: Handle making parts of dialogBinding.dialogMessage clickable (Privacy Policy/Terms)
                // This requires using SpannableString and ClickableSpan

            }
            notificationDialog?.show()
        }



        private fun performSearch(query: String) {

            viewModel.CitySearch(query)
        }

        override fun onDestroyView() {
            super.onDestroyView()
            dismissLocatingDialog() // Ensure dialogs are dismissed
            notificationDialog?.dismiss()
            locatingDialog = null
            notificationDialog = null
            _binding = null // Avoid memory leaks
        }
    }


