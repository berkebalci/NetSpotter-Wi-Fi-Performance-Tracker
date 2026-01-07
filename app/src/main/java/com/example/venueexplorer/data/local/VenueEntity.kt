// data/local/VenueEntity.kt
package com.example.venueexplorer.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "venues",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Kategori silinirse mekanlar da silinir
        )
    ],
    indices = [Index(value = ["categoryId"])] // Foreign key performansı için
)
data class VenueEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val description: String,
    val rating: Float, // ✅ Float olmalı (API ile uyumlu)

    // ✅ Sadece categoryId yeterli (JOIN ile category bilgisi alınacak)
    val categoryId: String,

    // GeoLocation (Nullable - şu an kullanılmıyor ama ileride lazım olabilir)
    val latitude: Double? = null,
    val longitude: Double? = null
)