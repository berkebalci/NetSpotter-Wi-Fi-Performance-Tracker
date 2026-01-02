package com.example.venueexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venues")
data class VenueEntity(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int,
    val title: Int,
    val description: String,
    val rating: Int,
    val categoryName: String,
    val latitude: Double?,
    val longitude: Double?
)
