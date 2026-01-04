package com.example.venueexplorer.presentation.state

import com.example.venueexplorer.data.local.CategoryEntity
import com.example.venueexplorer.data.local.VenueEntity
import com.example.venueexplorer.data.model.Category
import com.example.venueexplorer.data.model.Venue

data class HomeUIState(
    val venues: List<VenueEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
