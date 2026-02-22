package com.example.venueexplorer.presentation.state

import com.google.maps.android.compose.MarkerState

/**
 * MapScreen için UI state data class'ı.
 *
 * ViewModel bu state'i StateFlow olarak yayınlar.
 * Composable sadece bu state'i observe eder — konum mantığı hakkında
 * hiçbir şey bilmek zorunda değildir.
 *
 * Configuration change (ekran dönme vb.) sırasında ViewModel hayatta kalır,
 * dolayısıyla bu state de korunur. `remember { mutableStateOf() }` kullanırsak
 * bu koruma olmaz.
 */
data class MapUiState(
    /** Kullanıcının mevcut konumu. null → henüz alınmadı veya izin yok. */
    val userMarkerState: MarkerState? = null,
    /** Konum alınıyor mu? Yükleniyor göstergesi için. */
    val isLoadingLocation: Boolean = false
)
