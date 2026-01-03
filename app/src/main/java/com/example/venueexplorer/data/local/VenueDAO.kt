package com.example.venueexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VenueDAO {

    // Dönüş tipi List<VenueEntity> olmalı
    @Query("SELECT * from venues")
    suspend fun getAllVenues(): List<VenueEntity>

    // ID String olmalı (MongoDB ObjectId)
    @Query("SELECT * from venues WHERE id = :id")
    suspend fun getVenueById(id: String): VenueEntity?

    // Tekli Ekleme
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenue(venueEntity: VenueEntity)

    // Çoklu Ekleme (Repository'de bunu kullanıyoruz)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(venues: List<VenueEntity>)

    // ID String olmalı
    @Query("DELETE FROM venues WHERE id = :id")
    suspend fun deleteVenueById(id: String)

    @Query("DELETE FROM venues")
    suspend fun deleteAllVenues()
}