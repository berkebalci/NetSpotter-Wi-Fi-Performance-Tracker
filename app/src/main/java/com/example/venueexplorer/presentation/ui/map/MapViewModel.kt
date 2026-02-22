package com.example.venueexplorer.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venueexplorer.domain.usecase.GetCurrentLocationUseCase
import com.example.venueexplorer.presentation.state.MapUiState
import com.google.maps.android.compose.MarkerState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MapScreen için ViewModel.
 *
 * Sorumlulukları:
 * - Konum izninin verildiğini UI'dan alarak Use Case'i tetiklemek
 * - Konum yükleme durumunu (loading, sonuç) StateFlow ile UI'a iletmek
 * - Configuration change (ekran dönme) sırasında state'i korumak
 *
 * UI bilmez ki:
 * - Konumu kim sağlıyor (FusedLocationProviderClient mi, mock mu?)
 * - Hangi cascade stratejisi uygulanıyor
 * - Coroutine'ler nasıl yönetiliyor
 */
class MapViewModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /**
     * İzin verildiğinde UI bu fonksiyonu çağırır.
     * ViewModel viewModelScope içinde konumu async olarak alır ve state'i günceller.
     * UI bu süreç hakkında hiçbir şey bilmez; sadece uiState'i observe eder.
     */
    fun onPermissionGranted() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }

            val location = getCurrentLocationUseCase()

            _uiState.update { currentState ->
                currentState.copy(
                    isLoadingLocation = false,
                    userMarkerState = location?.let {
                        MarkerState(position = LatLng(it.latitude, it.longitude))
                    }
                )
            }
        }
    }
}
