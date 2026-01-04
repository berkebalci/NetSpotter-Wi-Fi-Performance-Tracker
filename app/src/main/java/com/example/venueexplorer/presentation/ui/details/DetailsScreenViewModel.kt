package com.example.venueexplorer.presentation.ui.details

import androidx.lifecycle.ViewModel
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueLocalRepository

class DetailsScreenViewModel(
    private val venueRepository: VenueLocalRepository,
    private val categoryRepository: CategoryLocalRepository,
    ) : ViewModel(){

}