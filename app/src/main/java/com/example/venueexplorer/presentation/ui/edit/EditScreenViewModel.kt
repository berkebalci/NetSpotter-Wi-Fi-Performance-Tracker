package com.example.venueexplorer.presentation.ui.edit

import androidx.lifecycle.ViewModel
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueLocalRepository

class EditScreenViewModel(
    private val venueRepository: VenueLocalRepository,
    private val categoryRepository: CategoryLocalRepository
) : ViewModel() {

}