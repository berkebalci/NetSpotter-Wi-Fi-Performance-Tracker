package com.example.venueexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

//TODO: Burasi duzeltilecek eksik parametre var
@Entity(tableName = "venues")
data class VenueEntity(
    @PrimaryKey
    val id: String,

    val title: String,
    val description: String,
    val rating: Int,

    // İLİŞKİSEL VERİ TAKTİĞİ:
    // Kategori tablosuyla Join yapmak yerine, okuması hızlı olsun diye
    // gerekli bilgileri buraya da kaydediyoruz (Denormalization).
    val categoryId: String,
    val categoryName: String,
    val categoryColor: String,

    // GeoLocation için (Nullable yaptık, boş gelebilir)
    val latitude: Double? = null,
    val longitude: Double? = null
)
