package com.example.venueexplorer.data.local

import android.content.Context
import com.example.venueexplorer.data.remote.VenueExplorerApiService
import com.example.venueexplorer.data.remote.VenueExplorerClient

class AppContainer(private val context: Context) {
    private val database : AppDataBase by lazy {  //bu bizim singelton database objemiz olcak
        AppDataBase.getDatabase(context)
    }

    private val apiService : VenueExplorerApiService by lazy {
        VenueExplorerClient.apiService
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