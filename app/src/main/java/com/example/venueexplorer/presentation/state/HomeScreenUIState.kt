package com.example.venueexplorer.presentation.state

import com.example.venueexplorer.data.local.CategoryEntity
import com.example.venueexplorer.data.local.VenueEntity

data class HomeScreenUIState(
    val venues: List<VenueEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)
