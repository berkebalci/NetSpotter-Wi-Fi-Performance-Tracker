package com.example.venueexplorer.data.local

import android.content.Context
import com.example.venueexplorer.data.location.LocationService
import com.example.venueexplorer.data.remote.VenueExplorerApiService
import com.example.venueexplorer.data.remote.VenueExplorerClient
import com.example.venueexplorer.data.repository.SpeedTestRepository
import com.example.venueexplorer.data.repository.SpeedTestRepositoryImpl
import com.example.venueexplorer.di.NetworkModule
import com.example.venueexplorer.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AppContainer(private val context: Context) {
    private val database : AppDataBase by lazy {  //bu bizim singelton database objemiz olcak
        AppDataBase.getDatabase(context)
    }

    private val apiService : VenueExplorerApiService by lazy {
        VenueExplorerClient.apiService
    }
    // LocationService, LocationRepository interface'ini implement eder.
    // Burada LocationRepository tipiyle expose ediyoruz (Dependency Inversion Principle):
    // Yukarı katmanlar LocationService'e değil, interface'e bağımlı olur.
    val locationRepository: LocationRepository by lazy {
        LocationService(fusedLocationProviderClient, context)
    }
    val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 3. Repository'leri Oluştur
    // VenueLocalRepository, çalışmak için API ve DAO'ya ihtiyaç duyar.
    // Container bu parçaları birleştirip Repository'yi hazırlar.
    val venueLocalRepository: VenueLocalRepository by lazy {
        VenueLocalRepository(apiService, database.venueDao())
    }

    // CategoryRepository aynı mantıkla oluşturulur.
    val categoryRepository: CategoryLocalRepository by lazy {
        CategoryLocalRepository(apiService, database.categoryDao())
    }
    
    // SpeedTestRepository for internet speed testing
    // Inject OkHttpClient from NetworkModule for better testability and configuration management
    val speedTestRepository: SpeedTestRepository by lazy {
        SpeedTestRepositoryImpl(NetworkModule.provideSpeedTestClient())
    }
}