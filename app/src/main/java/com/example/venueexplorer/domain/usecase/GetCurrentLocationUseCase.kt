package com.example.venueexplorer.domain.usecase

import android.location.Location
import android.util.Log
import com.example.venueexplorer.domain.repository.LocationRepository
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Kullanıcının mevcut konumunu almaya yarayan Use Case.
 *
 * Sorumluluk: "Konumu nasıl alacağım?" iş mantığını tek bir yerde toplar.
 *
 * Strateji (Google'ın önerdiği cascading yaklaşım):
 * 1. getCurrentLocation() → Hızlı, anlık konum (GPS açıksa çok iyi)
 * 2. Null veya hata varsa → requestSingleLocationUpdate() (en güvenilir)
 * 3. O da null dönerse → getLastLocation() (son çare, cache'lenmiş konum)
 *
 * ViewModel bu Use Case'i çağırır ve sadece Location? sonucunu alır.
 * Cascade mantığı hakkında hiçbir şey bilmek zorunda değildir.
 *
 * invoke() operatörü sayesinde ViewModel'da kullanımı: `getLocationUseCase()`
 */
class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {
    private val tag = "GetCurrentLocationUseCase"

    suspend operator fun invoke(): Location? {
        if (!locationRepository.hasLocationPermission()) {
            Log.w(tag, "Location permission not granted")
            return null
        }

        // Adım 1: Hızlı yol — getCurrentLocation
        val quickLocation = tryGetCurrentLocation()
        if (quickLocation != null) {
            Log.d(tag, "getCurrentLocation succeeded: lat=${quickLocation.latitude}, lng=${quickLocation.longitude}")
            return quickLocation
        }

        // Adım 2: Güvenilir yol — requestSingleLocationUpdate (coroutine-friendly wrapper)
        Log.d(tag, "getCurrentLocation returned null, trying requestSingleLocationUpdate")
        val updateLocation = tryRequestSingleUpdate()
        if (updateLocation != null) {
            Log.d(tag, "requestSingleLocationUpdate succeeded: lat=${updateLocation.latitude}, lng=${updateLocation.longitude}")
            return updateLocation
        }

        // Adım 3: Son çare — getLastLocation (cache'lenmiş konum)
        Log.w(tag, "requestSingleLocationUpdate returned null, trying getLastLocation as fallback")
        val lastLocation = tryGetLastLocation()
        if (lastLocation != null) {
            Log.d(tag, "getLastLocation succeeded: lat=${lastLocation.latitude}, lng=${lastLocation.longitude}")
        } else {
            Log.e(tag, "All location methods failed, returning null")
        }
        return lastLocation
    }

    /** Task<Location> tabanlı getCurrentLocation()'ı coroutine'e çevirir. */
    private suspend fun tryGetCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                locationRepository.getCurrentLocation()
                    .addOnSuccessListener { location -> continuation.resume(location) }
                    .addOnFailureListener { continuation.resume(null) }
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException in getCurrentLocation: ${e.message}")
                continuation.resume(null)
            }
        }

    /** Callback tabanlı requestSingleLocationUpdate()'i coroutine'e çevirir. */
    private suspend fun tryRequestSingleUpdate(): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                locationRepository.requestSingleLocationUpdate(
                    onLocationReceived = { location -> continuation.resume(location) },
                    timeoutMillis = 15000L
                )
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException in requestSingleLocationUpdate: ${e.message}")
                continuation.resume(null)
            }
        }

    /** Task<Location?> tabanlı getLastLocation()'ı coroutine'e çevirir. */
    private suspend fun tryGetLastLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                locationRepository.getLastLocation()
                    .addOnSuccessListener { location -> continuation.resume(location) }
                    .addOnFailureListener { continuation.resume(null) }
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException in getLastLocation: ${e.message}")
                continuation.resume(null)
            }
        }
}
