package com.example.venueexplorer.data.remote

import com.example.venueexplorer.data.model.Category
import com.example.venueexplorer.data.model.VenueRequest
import com.example.venueexplorer.data.model.VenueResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VenueExplorerApiService {

    // DÜZELTME: Artık VenueEntity yerine API modeli olan VenueResponse dönüyor
    @GET("api/venues")
    suspend fun getAllVenues(): List<VenueResponse>

    @GET("api/venues/{id}")
    suspend fun getVenueById(@Path("id") id: String): VenueResponse

    @GET("api/venues/search")
    suspend fun searchVenues(@Query("q") query: String): List<VenueResponse>
    //response olmayinca kendisi direkt 200 oldugu zaman sadece gonderiyor, olmadigi zaman da http error'u firlatiyor.

    // Ekleme yaparken de API modeli yolluyoruz
    @POST("api/venues")
    suspend fun addVenue(@Body venueRequest: VenueRequest): Response<VenueResponse>

    @PUT("api/venues/{id}")
    suspend fun updateVenue(@Path("id") id: String, @Body venueRequest: VenueRequest): Response<VenueResponse>

    @DELETE("api/venues/{id}")
    suspend fun deleteVenue(@Path("id") id: String): Response<Unit>

    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: String): Category
}