package com.example.venueexplorer.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.example.venueexplorer.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task

/**
 * LocationRepository interface'inin data katmanı implementasyonu.
 *
 * Bu sınıf FusedLocationProviderClient'ı sarmalayarak domain katmanının
 * beklediği soyut sözleşmeyi (LocationRepository) somut API çağrılarına dönüştürür.
 * ViewModel'lar ve UseCase'ler bu sınıfı DOĞRUDAN değil, LocationRepository
 * interface'i üzerinden kullanır.
 */
class LocationRepositoryImpl(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val context: Context
) : LocationRepository {

    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getLastLocation(): Task<Location?> {
        return fusedLocationClient.lastLocation
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCurrentLocation(): Task<Location> {
        return fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null // CancellationToken
        )
    }

    /**
     * Google'ın önerdiği en iyi pratik: requestLocationUpdates kullanarak
     * tek bir konum güncellemesi iste. Bu yöntem getCurrentLocation()'dan
     * daha güvenilirdir çünkü GPS aktif değilse bile çalışır.
     *
     * @param onLocationReceived Konum alındığında veya timeout olduğunda çağrılır
     * @param timeoutMillis Timeout süresi (milisaniye). Varsayılan: 15 saniye
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun requestSingleLocationUpdate(
        onLocationReceived: (Location?) -> Unit,
        timeoutMillis: Long
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // Interval: 10 saniye
        )
            .setMaxUpdateDelayMillis(5000L) // Maksimum gecikme: 5 saniye
            .setWaitForAccurateLocation(false) // Hızlı yanıt için false
            .build()

        val handler = Handler(Looper.getMainLooper())

        // timeoutRunnable'ı önce tanımla ki locationCallback içinde erişilebilsin
        var locationCallback: LocationCallback? = null
        val timeoutRunnable = Runnable {
            // Timeout oldu, callback'i temizle ve null döndür
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            onLocationReceived(null)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Konum alındı, callback'i temizle ve sonucu döndür
                fusedLocationClient.removeLocationUpdates(this)
                handler.removeCallbacks(timeoutRunnable)
                onLocationReceived(locationResult.lastLocation)
            }
        }

        try {
            // Konum güncellemelerini başlat
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Timeout mekanizması ekle
            handler.postDelayed(timeoutRunnable, timeoutMillis)
        } catch (e: SecurityException) {
            handler.removeCallbacks(timeoutRunnable)
            onLocationReceived(null)
        }
    }


}