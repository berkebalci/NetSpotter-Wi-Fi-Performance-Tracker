package com.example.venueexplorer.data.remote

import com.example.venueexplorer.di.NetworkModule

object VenueExplorerClient {
    // Emulator için 10.0.2.2 kullanıyoruz
    // It works on the real device !!

    val apiService: VenueExplorerApiService by lazy {
        NetworkModule.provideRetrofitClient()
            .create(VenueExplorerApiService::class.java)
    }
}