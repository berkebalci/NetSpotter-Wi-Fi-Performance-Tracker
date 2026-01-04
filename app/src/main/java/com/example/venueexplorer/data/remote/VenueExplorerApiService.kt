package com.example.venueexplorer.data.remote

import com.example.venueexplorer.data.model.Category
import com.example.venueexplorer.data.model.Venue
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueExplorerApiService {

    // DÜZELTME: Artık VenueEntity yerine API modeli olan Venue dönüyor
    @GET("api/venues")
    suspend fun getAllVenues(): List<Venue>

    @GET("api/venues/{id}")
    suspend fun getVenueById(@Path("id") id: String): Venue

    @GET("api/venues/search")
    suspend fun searchVenues(@Query("q") query: String): List<Venue>
    //response olmayinca kendisi direkt 200 oldugu zaman sadece gonderiyor, olmadigi zaman da http error'u firlatiyor.

    // Ekleme yaparken de API modeli yolluyoruz
    @POST("api/venues")
    suspend fun addVenue(@Body venue: Venue): Response<Venue>

    @PUT("api/venues/{id}")
    suspend fun updateVenue(@Path("id") id: String, @Body venue: Venue): Response<Venue>

    @DELETE("api/venues/{id}")
    suspend fun deleteVenue(@Path("id") id: String): Response<Unit>

    @GET("api/categories")
    suspend fun getCategories(): List<Category>
}