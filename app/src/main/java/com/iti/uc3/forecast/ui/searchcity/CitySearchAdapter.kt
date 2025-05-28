package com.iti.uc3.forecast.ui.searchcity

import com.iti.uc3.forecast.databinding.ItemCitySearchResultBinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.uc3.forecast.data.network.api.dto.GeoLocation

class CitySearchAdapter(
    private val onItemClicked: (GeoLocation) -> Unit
) : ListAdapter<GeoLocation, CitySearchAdapter.CityViewHolder>(CityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCitySearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CityViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = getItem(position)
        holder.bind(city)
    }

    class CityViewHolder(
        private val binding: ItemCitySearchResultBinding,
        private val onItemClicked: (GeoLocation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: GeoLocation) {
            binding.cityResultText.text = city.name+ ", " + city.state+ ", " + city.country
            // You can set the location pin icon dynamically if needed
            // binding.locationPinIcon.setImageResource(R.drawable.some_other_icon)

            binding.root.setOnClickListener {
                onItemClicked(city)
            }
        }
    }

    // DiffUtil helps efficiently update the list
    class CityDiffCallback : DiffUtil.ItemCallback<GeoLocation>() {
        override fun areItemsTheSame(oldItem: GeoLocation, newItem: GeoLocation): Boolean {
            // Assuming name is a unique identifier for simplicity
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: GeoLocation, newItem: GeoLocation): Boolean {
            return oldItem == newItem
        }
    }
}

