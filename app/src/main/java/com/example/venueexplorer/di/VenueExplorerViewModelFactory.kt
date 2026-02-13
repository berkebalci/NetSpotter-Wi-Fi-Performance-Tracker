package com.example.venueexplorer.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueLocalRepository
import com.example.venueexplorer.data.location.LocationService
import com.example.venueexplorer.data.repository.SpeedTestRepository
import com.example.venueexplorer.presentation.ui.details.DetailsScreenViewModel
import com.example.venueexplorer.presentation.ui.edit.EditScreenViewModel
import com.example.venueexplorer.presentation.home.HomeScreenViewModel
import com.example.venueexplorer.presentation.ui.speedtest.SpeedTestViewModel


class VenueExplorerViewModelFactory(
    private val venueRepository: VenueLocalRepository,
    private val categoryRepository: CategoryLocalRepository,
    private val locationService: LocationService,
    private val speedTestRepository: SpeedTestRepository
): ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeScreenViewModel::class.java)){
            return HomeScreenViewModel(venueRepository, categoryRepository) as T
        }

        if(modelClass.isAssignableFrom(EditScreenViewModel:: class.java)){
            return EditScreenViewModel(venueRepository, categoryRepository,locationService) as T

        }
        if(modelClass.isAssignableFrom(DetailsScreenViewModel:: class.java)){
            return DetailsScreenViewModel(venueRepository, categoryRepository) as T
        }
        
        if(modelClass.isAssignableFrom(SpeedTestViewModel::class.java)){
            return SpeedTestViewModel(speedTestRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")

    }
}