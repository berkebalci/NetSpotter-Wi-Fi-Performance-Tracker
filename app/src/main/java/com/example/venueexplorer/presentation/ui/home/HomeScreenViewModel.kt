package com.example.venueexplorer.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venueexplorer.data.local.CategoryEntity
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueEntity
import com.example.venueexplorer.data.local.VenueLocalRepository
import com.example.venueexplorer.presentation.state.HomeUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel (
    private val venuerepository: VenueLocalRepository,
    private val categoryrepository: CategoryLocalRepository
): ViewModel() {

    private val _homeUIState = MutableStateFlow(HomeUIState())
    val homeUIState = _homeUIState.asStateFlow()

    init{
        loadScreen()
    }
    fun loadScreen(){
        viewModelScope.launch {
            try {
                _homeUIState.update {it.copy(isLoading = true)
                }
                val venues: List<VenueEntity> = venuerepository.getVenues()
                val categories : List<CategoryEntity> = categoryrepository.getAllCategories()

                if(venues.isNotEmpty() && categories.isNotEmpty() ){
                    _homeUIState.update {
                        it.copy(
                            venues = venues,
                            categories = categories,
                            isLoading = false
                        )
                    }
                }
            }
            catch (e: Exception){
                Log.e("HomeScreenViewModel", "Hata: ${e.message}")
                throw e
            }
        }
        fun searchVenue(query: String){
            viewModelScope.launch {
                try {
                    _homeUIState.update {
                        it.copy(isLoading = true)}
                    if(query.isNotBlank()){
                        val venues = venuerepository.searchVenues(query)
                        _homeUIState.update {
                            it.copy(
                                venues = venues,
                                isLoading = false
                            )
                        }
                    }
                    else{
                        val venues = venuerepository.getVenues()
                        _homeUIState.update {
                            it.copy(
                                venues = venues,
                                isLoading = false
                            )
                        }
                    }
                }
                catch (e: Exception){
                    Log.e("HomeScreenViewModel", "Hata: ${e.message}")
                    throw e
                }
            }
        }
    }

}