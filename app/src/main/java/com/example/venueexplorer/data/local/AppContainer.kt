package com.example.venueexplorer.data.local

import android.content.Context
import com.example.venueexplorer.data.remote.VenueExplorerApiService
import com.example.venueexplorer.data.remote.VenueExplorerClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AppContainer(private val context: Context) {
    private val database : AppDataBase by lazy {  //bu bizim singelton database objemiz olcak
        AppDataBase.getDatabase(context)
    }

    private val apiService : VenueExplorerApiService by lazy {
        VenueExplorerClient.apiService
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
}