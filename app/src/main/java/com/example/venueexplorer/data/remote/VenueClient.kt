package com.example.venueexplorer.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object VenueClient {
    // Emulator için 10.0.2.2 kullanıyoruz
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val apiService: VenueApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // DÜZELTME: JSON işlemleri için GsonConverterFactory şarttır!
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VenueApiService::class.java)
    }
}