package com.example.venueexplorer.data.local

import com.adsoyad.venueexplorer.data.VenueApiService
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object VenueClient{
    private const val base_url = "http://10.0.2.2:3000/"

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(base_url)
        .build()
        .create(VenueApiService :: class.java) // bu sayede interface'imiz
}