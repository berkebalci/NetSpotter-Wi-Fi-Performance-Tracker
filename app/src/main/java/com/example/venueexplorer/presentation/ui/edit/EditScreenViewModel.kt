package com.example.venueexplorer.presentation.ui.edit
import android.content.ContentValues.TAG
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venueexplorer.data.local.CategoryLocalRepository
import com.example.venueexplorer.data.local.VenueLocalRepository
import com.example.venueexplorer.data.model.VenueRequest
import com.example.venueexplorer.presentation.state.EditScreenUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

class EditScreenViewModel(
    private val venueLocalRepository: VenueLocalRepository,
    private val categoryLocalRepository: CategoryLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditScreenUIState())
    val uiState: StateFlow<EditScreenUIState> = _uiState.asStateFlow()
    val hasLocationPermissionGranted by mutableStateOf(false)
    val hasLocationPermissionDenied by mutableStateOf(false)

    init {
        loadCategories()

    }

    // ═══════════════════════════════════════════════════════
    // LOAD CATEGORIES
    // ═══════════════════════════════════════════════════════
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val categories = categoryLocalRepository.getAllCategories()

                if (categories.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Kategoriler yüklenemedi. Lütfen internet bağlantınızı kontrol edin."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Kategoriler yüklenemedi: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // LOAD VENUE FOR EDITING
    // ═══════════════════════════════════════════════════════
    fun loadVenueForEditing(venueId: String) {

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true, venueId = venueId) }

            try {

                val venue = venueLocalRepository.getVenueById(venueId)

                if (venue != null) {
                    _uiState.update {
                        it.copy(
                            title = venue.title,
                            description = venue.description,
                            rating = venue.rating,
                            selectedCategoryId = venue.categoryId,
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
                        errorMessage = "Mekan yüklenemedi: ${e.message}"
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // UPDATE FORM FIELDS
    // ═══════════════════════════════════════════════════════
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateRating(rating: Float) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    // ═══════════════════════════════════════════════════════
    // SAVE VENUE (ADD or UPDATE)
    // ═══════════════════════════════════════════════════════
    fun saveVenue() {
        val state = _uiState.value

        // Validation
        if (state.title.isBlank()) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Başlık boş olamaz"
                )
            }
            return
        }

        if (state.title.length < 3) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Başlık en az 3 karakter olmalı"
                )
            }
            return
        }

        if (state.selectedCategoryId == null) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Lütfen bir kategori seçin"
                )
            }
            return
        }

        if (state.rating < 0 || state.rating > 5) {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "Puan 0-5 arasında olmalı"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, isError = false) }

            try {
                // Seçilen kategoriyi bul
                val selectedCategory = state.categories.find { it.id == state.selectedCategoryId }

                if (selectedCategory == null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isError = true,
                            errorMessage = "Seçilen kategori bulunamadı"
                        )
                    }
                    return@launch
                }

                // API modeli oluştur
                val venueRequest = VenueRequest(
                    title = state.title.trim(),
                    description = state.description.trim(),
                    rating = state.rating,
                    categoryId = selectedCategory.id,
                    latitude = state.latitude,
                    longitude = state.longitude
                    )


                if (state.isEditMode && state.venueId != null) {
                    // UPDATE
                    Log.e(TAG, "VenueLocal repo UPDATE VENUE calisti")
                    venueLocalRepository.updateVenues(state.venueId, venueRequest)
                } else {
                    // INSERT
                    Log.e(TAG, state.isEditMode.toString())

                    Log.e(TAG, "VenueLocal repo add venue calisti")
                    venueLocalRepository.addVenue(venueRequest)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isError = true,
                        errorMessage = "Kayıt hatası: ${e.message}"
                    )
                }
            }
        }
    }


    fun clearError() {
        _uiState.update { it.copy(isError = false, errorMessage = null) }
    }

    fun resetSavedState() {
        _uiState.update { it.copy(isSaved = false) }
    }
}