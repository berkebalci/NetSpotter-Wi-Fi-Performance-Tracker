package com.example.venueexplorer.data.local

import android.util.Log
// Model sınıfını import etmelisin (Dosya yolu farklı olabilir)
import com.example.venueexplorer.data.model.Venue
import com.example.venueexplorer.data.remote.VenueExplorerApiService

class VenueLocalRepository(
    private val api: VenueExplorerApiService,
    private val venueDao: VenueDAO
) {

    private val TAG = "VenueRepo"

    suspend fun getVenues(): List<VenueEntity> {
        try {
            val remoteVenues = api.getAllVenues() // List<Venue> döner
            if (remoteVenues.isNotEmpty()) {
                // Venue -> VenueEntity dönüşümü
                val entities = remoteVenues.map { it.toEntity() }

                venueDao.deleteAllVenues()
                venueDao.insertAll(entities)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Offline Mod Aktif: ${e.message}")
        }
        return venueDao.getAllVenues()
    }

    suspend fun addVenue(venue: Venue) {
        try {
            val responsee = api.addVenue(venue)
            if(responsee.isSuccessful){
                val addedVenue = responsee.body()
                venueDao.insertAll(listOf(addedVenue!!.toEntity()))

            }
            else{
                throw Exception("Ekleme başarısız: ${responsee.code()}")
            }
            // Tek bir elemanı listeye koyup ekliyoruz
        } catch (e: Exception) {
            Log.e(TAG, "Ekleme Hatası: ${e.message}")
            throw e
        }
    }

    suspend fun deleteVenue(id: String) {
        try {
            val response = api.deleteVenue(id)
            if (response.isSuccessful) {
                venueDao.deleteVenueById(id)
            } else {
                Log.e(TAG, "Silme başarısız: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Silme Hatası: ${e.message}")
            throw e
        }
    }
    suspend fun updateVenues(id: String,venue: Venue) {
        try {
            val response = api.updateVenue(id, venue)
            if (response.isSuccessful) {
                val updatedVenue = response.body()!!
                venueDao.updateVenue(updatedVenue.toEntity())
            } else {
                throw Exception("Güncelleme başarısız: ${response.code()}")
            }
        } catch (e: Exception){
            throw e
        }
    }

    suspend fun searchVenues(query: String): List<VenueEntity> {
        return try {
            val searchResults = api.searchVenues(query)
            searchResults.map { it.toEntity() }
        } catch (e: Exception) {
            Log.e(TAG, "Arama Hatası: ${e.message}")
            emptyList()
        }
    }

    // --- MAPPER DÜZELTİLDİ ---
    // Bu fonksiyon VenueEntity üzerinde değil, API'den gelen 'Venue' sınıfı üzerinde olmalı.
    private fun Venue.toEntity(): VenueEntity {
        return VenueEntity(
            id = id,
            title = title,
            description = description,
            rating = rating,
            // Venue modelinde 'category' objesi nullable olabilir (?.)
            categoryId = category?.id ?: "",
            latitude = null,
            longitude = null
        )
    }
}