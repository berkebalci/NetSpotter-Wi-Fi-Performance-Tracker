import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
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
    //var isLocationPermitted = remember { false } bu degiskenin degeri degistigi zaman recompose olmaz!!
    val context = LocalContext.current
    var isLocationPermitted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // --- İZİN YÖNETİMİ ---
    // Composable sadece iznin verilip verilmediğini ViewModel'a bildirir.
    // "İzinle ne yapılır?" sorusunun cevabı ViewModel'da.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationPermitted = granted
        if (granted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        // İzin zaten verilmişse hemen konumu al
        if (isLocationPermitted) {
            viewModel.onPermissionGranted()
        }
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // --- HARİTA VE KAMERA AYARLARI ---
    val venueLocation = remember(venueLatitude, venueLongitude) {
        Log.e("MapScreen", "Venue location: $venueLatitude, $venueLongitude")
        if (venueLatitude != null && venueLongitude != null) LatLng(venueLatitude, venueLongitude)
        else null
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            venueLocation ?: LatLng(uiState.userLatitude?:41.0082,uiState.userLongitude?: 28.9784),
            15f
        )
    }
    val mapUiSettings = remember(isLocationPermitted){
        MapUiSettings(
            myLocationButtonEnabled = false, // Built-in buton overlay ile çakışıyor, custom FAB kullanıyoruz
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val mapProperties = remember(isLocationPermitted) {
        MapProperties(
            isMyLocationEnabled = isLocationPermitted // Kullanıcının güncel konumunda mavi nokta çıkarır
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
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
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

        // Custom My Location FAB — kullanıcı konumu varsa göster
        if (uiState.userLatitude != null && uiState.userLongitude != null) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                LatLng(uiState.userLatitude!!, uiState.userLongitude!!),
                                15f
                            ),
                            durationMs = 1000
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location"
                )
            }
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