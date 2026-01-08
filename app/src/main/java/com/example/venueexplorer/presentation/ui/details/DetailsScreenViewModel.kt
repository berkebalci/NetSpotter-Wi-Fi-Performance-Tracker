package com.example.venueexplorer.presentation.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueLocalRepository

// presentation/viewmodel/DetailsScreenViewModel.kt

import com.example.venueexplorer.presentation.state.DetailsScreenUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsScreenViewModel(
    private val venueLocalRepository: VenueLocalRepository,
    private val categoryLocalRepository: CategoryLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsScreenUIState())
    val uiState: StateFlow<DetailsScreenUIState> = _uiState.asStateFlow()

    // ═══════════════════════════════════════════════════════
    // LOAD VENUE DETAILS
    // ═══════════════════════════════════════════════════════
    fun loadVenueDetails(venueId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }

            try {
                // Room'dan tüm venue'ları çek
                val venue = venueLocalRepository.getVenueById(venueId)

                if (venue != null) {
                    // Kategorileri çek

                    val category = categoryLocalRepository.getCategoryById(venue.categoryId)

                    _uiState.update {
                        it.copy(
                            venue = venue,
                            category = category,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Mekan bulunamadı"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Mekan yüklenirken hata: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // DELETE VENUE
    // ═══════════════════════════════════════════════════════
    fun deleteVenue(venueId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                venueLocalRepository.deleteVenue(venueId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isError = true,
                        errorMessage = "Silme hatası: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(isError = false, errorMessage = null) }
    }
}