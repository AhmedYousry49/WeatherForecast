package com.iti.uc3.forecast.ui.managecity;


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.uc3.forecast.databinding.ItemManageCityBinding


class ManageCityAdapter(
        private val onItemClicked: (ManagedCity) -> Unit,
private val onSelectionChanged: (Set<Int>) -> Unit // Callback with selected city IDs
    ,private val onLongPress: () -> Unit
) : ListAdapter<ManagedCity, ManageCityAdapter.CityViewHolder>(CityDiffCallback()) {

private var isSelectionMode = false
private val selectedItems = mutableSetOf<Int>() // Store IDs of selected cities

fun toggleSelectionMode(enable: Boolean) {
    if (isSelectionMode == enable) return
            isSelectionMode = enable
    if (!isSelectionMode) {
        selectedItems.clear()
    }
    notifyDataSetChanged() // Rebind all views to show/hide elements
    onSelectionChanged(selectedItems)
}

fun getSelectedItems(): Set<Int> {
    return selectedItems.toSet()
}
    fun toggleSelectAll() {
        if (!isSelectionMode) return

        if (selectedItems.size == currentList.size) {
            // All selected, so clear selection
            selectedItems.clear()
        } else {
            // Not all selected, so select all
            currentList.forEach { selectedItems.add(it.id) }
        }

        notifyDataSetChanged()
        onSelectionChanged(selectedItems)
    }

fun selectAll() {
    if (!isSelectionMode) return
            currentList.forEach { selectedItems.add(it.id) }
    notifyDataSetChanged()
    onSelectionChanged(selectedItems)
}

fun clearSelection() {
    if (!isSelectionMode) return
            selectedItems.clear()
    notifyDataSetChanged()
    onSelectionChanged(selectedItems)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
    val binding = ItemManageCityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
    )
    return CityViewHolder(binding)
}

override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
    val city = getItem(position)
    holder.bind(city, isSelectionMode, selectedItems.contains(city.id),onLongPress) {
        handleItemClick(city)
    }
}

private fun handleItemClick(city: ManagedCity) {
    if (isSelectionMode) {
        if (selectedItems.contains(city.id)) {
            selectedItems.remove(city.id)
        } else {
            selectedItems.add(city.id)
        }
        notifyItemChanged(currentList.indexOf(city)) // Update just this item
        onSelectionChanged(selectedItems)
    } else {
        onItemClicked(city) // Handle normal click
    }
}

class CityViewHolder(private val binding: ItemManageCityBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(city: ManagedCity, isSelectionMode: Boolean, isSelected: Boolean, onLongPress: () -> Unit, onClick: () -> Unit) {
        binding.cityNameText.text = city.name
        binding.countryNameText.text = city.country
        binding.temperatureText.text = "${city.temperature}°C"


        binding.cityBackgroundImage.setImageResource(city.weatherIconResId)

        // TODO: Set background image based on city/weather
        // binding.cityBackgroundImage.setImageResource(city.backgroundImageResId)
        // TODO: Set weather icon based on city.weatherCondition
        // binding.weatherIcon.setImageResource(city.weatherIconResId)

        if(city.isCurrentLocation) {
            binding.locationPinIcon.visibility = View.VISIBLE
        } else {
            binding.locationPinIcon.visibility = View.GONE
        }
        if(city.hasNotification) {
            binding.notificationBellIcon.visibility = View.VISIBLE
        } else {
            binding.notificationBellIcon.visibility = View.GONE
        }

        if (isSelectionMode && !city.isCurrentLocation) {
            binding.dragHandleIcon.visibility = View.VISIBLE
            binding.selectionCheckbox.visibility = View.VISIBLE
            binding.locationPinIcon.visibility = View.GONE
            binding.notificationBellIcon.visibility = View.GONE
            binding.selectionCheckbox.isChecked = isSelected
        } else {
            binding.dragHandleIcon.visibility = View.GONE
            binding.selectionCheckbox.visibility = View.GONE
//            binding.locationPinIcon.visibility = View.VISIBLE // Or based on city.isCurrentLocation
//            binding.notificationBellIcon.visibility = View.VISIBLE // Or based on city.hasNotification
            binding.selectionCheckbox.isChecked = false
        }

        // Handle click on the whole item
        binding.root.setOnClickListener { onClick() }
        // Handle long press to toggle selection mode
        binding.root.setOnLongClickListener {
            onLongPress()
            true
        }
        // Prevent checkbox from consuming click if needed, handle via root click
        binding.selectionCheckbox.isClickable = false
    }
}

class CityDiffCallback : DiffUtil.ItemCallback<ManagedCity>() {
override fun areItemsTheSame(oldItem: ManagedCity, newItem: ManagedCity): Boolean {
    return oldItem.id == newItem.id // Use a unique ID
}

override fun areContentsTheSame(oldItem: ManagedCity, newItem: ManagedCity): Boolean {
    return oldItem == newItem
}
    }
            }