import android.Manifest
import android.location.Location
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
import com.example.venueexplorer.data.remote.LocationService
import com.google.android.gms.maps.CameraUpdateFactory // 1. EKLENDİ
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    venueLatitude: Double?,
    venueLongitude: Double?,
    onBackClicked: () -> Unit,
    locationService: LocationService
) {
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(locationService.hasLocationPermission()) }

    // ... İzin kodları aynı kalabilir ...
    // (Kod tekrarı olmasın diye burayı kısalttım, senin kodundaki logic doğru)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasPermission) {
            getCurrentLocation(locationService) { location ->
                location?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            getCurrentLocation(locationService) { location ->
                location?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // --- HARİTA VE KAMERA AYARLARI ---

    val venueLocation = remember(venueLatitude, venueLongitude) {
        if (venueLatitude != null && venueLongitude != null) {
            LatLng(venueLatitude, venueLongitude)
        } else null
    }

    // 2. DÜZELTME: rememberCameraPositionState kullanımı
    // Varsayılan olarak Venue varsa oraya, yoksa İstanbul'a baksın
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            venueLocation ?: LatLng(41.0082, 28.9784),
            15f
        )
    }

    // 3. DÜZELTME: Animasyonu LaunchedEffect ile tetiklemek
    // VenueLocation değiştiği an (veya null değilse) kamera oraya kaysın.
    LaunchedEffect(venueLocation) {
        venueLocation?.let {
            // CameraUpdateFactory kullanıyoruz
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1000 // İsteğe bağlı animasyon süresi
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
            // onMapLoaded içindeki logic'i LaunchedEffect'e taşıdık, burası temiz kaldı.
        ) {
            // Venue Marker
            venueLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Venue Location"
                )
            }

            // User Marker
            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Your Location",
                    // Marker rengini değiştirebilirsin karışmaması için
                    // icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
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

// Helper function aynı kalabilir
private fun getCurrentLocation(
    locationService: LocationService,
    onLocationReceived: (Location?) -> Unit
) {
    // Burada MissingPermission hatası verebilir, @SuppressLint eklemen gerekebilir
    try {
        locationService.getLastLocation()
            .addOnSuccessListener { location -> onLocationReceived(location) }
            .addOnFailureListener { onLocationReceived(null) }
    } catch (e: SecurityException) {
        onLocationReceived(null)
    }
}