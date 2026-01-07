// presentation/state/DetailsScreenUIState.kt
package com.example.venueexplorer.presentation.state

import com.example.venueexplorer.data.local.CategoryEntity
import com.example.venueexplorer.data.local.VenueEntity

data class DetailsScreenUIState(
    val venue: VenueEntity? = null,
    val category: CategoryEntity? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null
)