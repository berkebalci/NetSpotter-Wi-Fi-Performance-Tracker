package com.adsoyad.venueexplorer.data

import com.adsoyad.venueexplorer.data.local.entity.CategoryEntity
import com.adsoyad.venueexplorer.data.local.entity.VenueEntity

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueApiService {

    // 1. Tüm mekanları getir
    @GET("api/venues")
    suspend fun getAllVenues(): List<VenueEntity>

    // 2. ID'ye göre mekan getir
    @GET("api/venues/{id}")
    suspend fun getVenueById(@Path("id") id: String): VenueEntity

    // 3. Arama yap (Query parametresi: ?q=...)
    @GET("api/venues/search")
    suspend fun searchVenues(@Query("q") query: String): List<VenueEntity>

    // 4. Yeni mekan ekle (Body olarak JSON yolluyoruz)
    @POST("api/venues")
    suspend fun addVenue(@Body venue: VenueEntity): VenueEntity

    // 5. Mekan güncelle
    @PUT("api/venues/{id}")
    suspend fun updateVenue(@Path("id") id: String, @Body venue: VenueEntity): VenueEntity

    // 6. Mekan sil
    @DELETE("api/venues/{id}")
    suspend fun deleteVenue(@Path("id") id: String): Response<Unit>

    // 7. Kategorileri getir (Spinner doldurmak için)
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryEntity>
}