package com.example.venueexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VenueDAO {

    // Düzeltme: Fonksiyon ismi getAllVenues ve dönüş tipi List
    @Query("SELECT * from venues")
    suspend fun getAllVenues(): List<VenueEntity>

    // Düzeltme: ID String olmalı
    @Query("SELECT * from venues WHERE id = :id")
    suspend fun getVenueById(id: String): VenueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenue(venueEntity: VenueEntity)

    // Repository için gerekli toplu ekleme
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(venues: List<VenueEntity>)

    // Düzeltme: ID String olmalı
    @Query("DELETE FROM venues WHERE id = :id")
    suspend fun deleteVenueById(id: String)

    @Query("DELETE FROM venues")
    suspend fun deleteAllVenues()
}