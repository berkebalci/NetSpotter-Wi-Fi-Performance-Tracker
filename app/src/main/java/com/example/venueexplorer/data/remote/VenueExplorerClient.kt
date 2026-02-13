package com.example.venueexplorer.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object VenueExplorerClient {
    // Emulator için 10.0.2.2 kullanıyoruz
    private const val BASE_URL = "http://10.0.2.2:3000/"
    //It works on the real device !!

    val apiService: VenueExplorerApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // DÜZELTME: JSON işlemleri için GsonConverterFactory şarttır!
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VenueExplorerApiService::class.java)
    }
}