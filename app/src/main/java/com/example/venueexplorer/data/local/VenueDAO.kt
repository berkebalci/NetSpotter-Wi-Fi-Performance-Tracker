package com.example.venueexplorer.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VenueDAO {

    @Query("SELECT * from venues")
    suspend fun getallVenues()


    @Query("SELECT * from venues WHERE categoryId = :categoryId")
    fun getVenueById(categoryId: Int): VenueEntity?


    @Insert
    suspend fun insertVenue(venueEntity: VenueEntity)

    @Delete
    suspend fun deleteVenueById(id: Int)

    @Delete
    suspend fun deleteAllVenues()


}