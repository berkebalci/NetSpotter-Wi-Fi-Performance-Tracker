// presentation/viewmodel/HomeScreenViewModel.kt
package com.example.venueexplorer.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueLocalRepository
import com.example.venueexplorer.presentation.state.HomeScreenUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val venueLocalRepository: VenueLocalRepository,
    private val categoryRepository: CategoryLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUIState())
    val uiState: StateFlow<HomeScreenUIState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    // ═══════════════════════════════════════════════════════
    // API TEST 1: GET ALL VENUES & CATEGORIES
    // ═══════════════════════════════════════════════════════
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }

            try {
                // ✅ 1. ÖNCE kategorileri yükle
                val categories = categoryRepository.getAllCategories()
                _uiState.update { it.copy(categories = categories) }

                // ✅ 2. SONRA venue'ları yükle (artık foreign key'ler mevcut)
                val venues = venueLocalRepository.getVenues()
                _uiState.update {
                    it.copy(
                        venues = venues,
                        isLoading = false
                    )
                }


            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Veri yüklenirken hata: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // API TEST 2: DELETE VENUE
    // ═══════════════════════════════════════════════════════
    fun deleteVenue(venueId: String) {
        viewModelScope.launch {
            try {
                venueLocalRepository.deleteVenue(venueId)
                // Silme başarılı, listeyi yenile
                loadData()
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

    // ═══════════════════════════════════════════════════════
    // API TEST 3: SEARCH VENUES
    // ═══════════════════════════════════════════════════════
    fun searchVenues(query: String) {
        _uiState.update { it.copy(searchQuery = query, isLoading = true) }

        viewModelScope.launch {
            try {
                Log.e("Query Search", "Query: $query")
                if (query.isBlank()) {
                    // Arama boşsa tüm mekanları göster
                    loadData()
                } else {
                    // Arama yap
                    val results = venueLocalRepository.searchVenues(query)
                    _uiState.update {
                        it.copy(
                            venues = results,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Arama hatası: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // FILTER BY CATEGORY (Local filtreleme)
    // ═══════════════════════════════════════════════════════
    fun filterByCategory(categoryId: String?) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val allVenues = venueLocalRepository.getVenues()

                val filteredVenues = if (categoryId == null) {
                    allVenues
                } else {
                    allVenues.filter { it.categoryId == categoryId }
                }

                _uiState.update {
                    it.copy(
                        venues = filteredVenues,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Filtreleme hatası: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(isError = false, errorMessage = null) }
    }

    fun refreshData() {
        loadData()
    }
}