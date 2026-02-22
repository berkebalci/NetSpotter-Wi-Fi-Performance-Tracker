import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.venueexplorer.presentation.ui.map.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * MapScreen — Purely UI (Composable)
 *
 * Bu composable'ın iki tek sorumluluğu var:
 * 1. Kullanıcıdan izin istemek ve sonucu ViewModel'a iletmek
 * 2. ViewModel'dan gelen `uiState`'i haritada render etmek
 *
 * Konum NASIL alınır? → MapViewModel bilir
 * Konum alma stratejisi nedir? → GetCurrentLocationUseCase bilir
 * FusedLocationProviderClient nedir? → LocationRepositoryImpl bilir
 *
 * Bu composable bunların HiÇbirini bilmez.
 */
@Composable
fun MapScreen(
    venueLatitude: Double?,
    venueLongitude: Double?,
    onBackClicked: () -> Unit,
    viewModel: MapViewModel
) {
    // ViewModel'dan gelen state'i observe et
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // --- İZİN YÖNETİMİ ---
    // Composable sadece iznin verilip verilmediğini ViewModel'a bildirir.
    // "İzinle ne yapılır?" sorusunun cevabı ViewModel'da.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // --- HARİTA VE KAMERA AYARLARI ---
    val venueLocation = remember(venueLatitude, venueLongitude) {
        if (venueLatitude != null && venueLongitude != null) LatLng(venueLatitude, venueLongitude)
        else null
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            venueLocation ?: LatLng(41.0082, 28.9784),
            15f
        )
    }

    LaunchedEffect(venueLocation) {
        venueLocation?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1000
            )
        }
    }

    // --- UI RENDER ---
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            venueLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Venue Location"
                )
            }

            // Kullanıcı konumu: ViewModel'dan StateFlow aracılığıyla geliyor.
            // Configuration change'de kaybolmaz.
            uiState.userMarkerState?.let { markerState ->
                Marker(
                    state = markerState,
                    title = "Your Location"
                )
            }
        }

        // Yükleniyor göstergesi — konum alınırken
        if (uiState.isLoadingLocation) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Map View",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}