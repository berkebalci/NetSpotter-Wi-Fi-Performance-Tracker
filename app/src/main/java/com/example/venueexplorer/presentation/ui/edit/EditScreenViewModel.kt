package com.example.venueexplorer.presentation.ui.edit
import android.content.ContentValues.TAG
import android.location.Location
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
import androidx.compose.runtime.setValue
import com.example.venueexplorer.domain.repository.LocationRepository

class EditScreenViewModel(
    private val venueLocalRepository: VenueLocalRepository,
    private val categoryLocalRepository: CategoryLocalRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditScreenUIState())
    val uiState: StateFlow<EditScreenUIState> = _uiState.asStateFlow()
    var hasLocationPermissionGranted by mutableStateOf(false)

    init {
        loadCategories()
    }

    fun updateLocationPermssion(isGranted: Boolean){
        hasLocationPermissionGranted = isGranted
    }

    /** EditScreen'in doğrudan LocationRepository'e erişmeden izin durumunu sorgulamasını sağlar. */
    fun hasLocationPermission(): Boolean = locationRepository.hasLocationPermission()
    
    /**
     * Kullanıcının mevcut konumunu almak için Google'ın önerdiği en iyi pratikleri kullanır.
     * 
     * Strateji:
     * 1. Önce getCurrentLocation() dene (hızlı, eğer mevcut konum varsa)
     * 2. Eğer null dönerse, requestSingleLocationUpdate() kullan (daha güvenilir)
     * 3. Son çare olarak getLastLocation() dene (cache'lenmiş konum)
     */
    fun getCurrentLocation(
        onLocationReceived: (Location?) -> Unit
    ) {
        // İzin kontrolü
        if (!locationRepository.hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            onLocationReceived(null)
            return
        }

        try {
            // 1. Adım: getCurrentLocation() dene (hızlı yöntem)
            locationRepository.getCurrentLocation()
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Başarılı, konumu döndür
                        Log.d(TAG, "getCurrentLocation succeeded: lat=${location.latitude}, lng=${location.longitude}")
                        onLocationReceived(location)
                    } else {
                        // getCurrentLocation null döndü, requestLocationUpdates ile dene
                        Log.d(TAG, "getCurrentLocation returned null, trying requestSingleLocationUpdate")
                        requestLocationWithUpdates(onLocationReceived)
                    }
                }
                .addOnFailureListener { exception ->
                    // getCurrentLocation başarısız, requestLocationUpdates ile dene
                    Log.w(TAG, "getCurrentLocation failed: ${exception.message}, trying requestSingleLocationUpdate")
                    requestLocationWithUpdates(onLocationReceived)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in getCurrentLocation: ${e.message}")
            onLocationReceived(null)
        }
    }
    
    /**
     * requestLocationUpdates kullanarak konum almayı dener.
     * Bu Google'ın önerdiği en güvenilir yöntemdir.
     */
    private fun requestLocationWithUpdates(
        onLocationReceived: (Location?) -> Unit
    ) {
        try {
            locationRepository.requestSingleLocationUpdate(
                onLocationReceived = { location ->
                    if (location != null) {
                        Log.d(TAG, "requestSingleLocationUpdate succeeded: lat=${location.latitude}, lng=${location.longitude}")
                        onLocationReceived(location)
                    } else {
                        // requestLocationUpdates da başarısız, son çare olarak getLastLocation() dene
                        Log.w(TAG, "requestSingleLocationUpdate returned null, trying getLastLocation as fallback")
                        locationRepository.getLastLocation()
                            .addOnSuccessListener { lastLocation ->
                                if (lastLocation != null) {
                                    Log.d(TAG, "getLastLocation succeeded: lat=${lastLocation.latitude}, lng=${lastLocation.longitude}")
                                    onLocationReceived(lastLocation)
                                } else {
                                    Log.e(TAG, "All location methods failed, returning null")
                                    onLocationReceived(null)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "getLastLocation also failed: ${exception.message}")
                                onLocationReceived(null)
                            }
                    }
                },
                timeoutMillis = 15000L // 15 saniye timeout
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in requestLocationWithUpdates: ${e.message}")
            onLocationReceived(null)
        }
    }

    // ═══════════════════════════════════════════════════════
    // UPDATE LOCATION
    // ═══════════════════════════════════════════════════════
    fun updateLocation(latitude: Double?, longitude: Double?) {
        Log.e("","$latitude and $longitude")
        _uiState.update {
            it.copy(
                latitude = latitude,
                longitude = longitude
            )
        }
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
                            latitude = venue.latitude,
                            longitude = venue.longitude,
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