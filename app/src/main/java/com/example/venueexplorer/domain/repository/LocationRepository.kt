package com.example.venueexplorer.domain.repository

import android.location.Location
import com.google.android.gms.tasks.Task

/**
 * Konum işlemlerini soyutlayan domain katmanı interface'i.
 *
 * Bu interface sayesinde:
 * - UI ve ViewModel katmanları data katmanına (LocationRepositoryImpl, FusedLocationProviderClient)
 *   doğrudan bağımlı olmaz (Dependency Inversion Principle).
 * - Test sırasında gerçek GPS kullanmadan bu interface mock'lanabilir.
 * - İleride farklı bir konum kütüphanesine geçilirse sadece bu interface'in
 *   implementasyonu değişir, ViewModel ve UI hiç dokunulmaz.
 */
interface LocationRepository {

    /** Kullanıcının konum izninin verilip verilmediğini kontrol eder. */
    fun hasLocationPermission(): Boolean

    /**
     * FusedLocationProviderClient'ın cache'lediği son konumu döndürür.
     * GPS hiç açılmamışsa null gelebilir. Task tabanlıdır.
     */
    fun getLastLocation(): Task<Location?>

    /**
     * Anlık olarak tek bir konum güncellemesi ister.
     * Android'in Fused Location Provider'dan doğrudan mevcut konumu alır.
     * getLastLocation()'dan daha güncel, ama GPS kapalıysa null dönebilir.
     * Task tabanlıdır.
     */
    fun getCurrentLocation(): Task<Location>

    /**
     * requestLocationUpdates + timeout mekanizması ile konum alır.
     * GPS kapalı olsa bile çalışır; bu nedenle en güvenilir yöntemdir.
     * Callback tabanlıdır.
     */
    fun requestSingleLocationUpdate(
        onLocationReceived: (Location?) -> Unit,
        timeoutMillis: Long = 15000L
    )
}
