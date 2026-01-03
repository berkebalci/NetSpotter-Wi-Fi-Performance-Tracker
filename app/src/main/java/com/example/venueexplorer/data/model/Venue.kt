package com.example.venueexplorer.data.model

import com.google.gson.annotations.SerializedName

// API'den gelen ham veri yapısı
data class Venue(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("rating")
    val rating: Int,

    // İŞTE FARK BURADA:
    // API, kategoriyi bir obje olarak yollar.
    // Entity (Veritabanı) ise bunu parçalanmış stringler olarak tutar.
    @SerializedName("categoryId")
    val category: Category? // Bazen null gelebilir, o yüzden ? koyduk
)