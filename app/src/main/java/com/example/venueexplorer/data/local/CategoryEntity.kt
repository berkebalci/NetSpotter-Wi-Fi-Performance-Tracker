package com.example.venueexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String, // MongoDB _id'si buraya gelecek
    val name: String,
    val color: String,
    val iconName: String
)