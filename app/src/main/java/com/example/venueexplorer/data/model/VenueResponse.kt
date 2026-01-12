package com.example.venueexplorer.data.model

import com.google.gson.annotations.SerializedName

// API'den gelen ham veri yapısı
data class VenueResponse(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("rating")
    val rating: Float,

    // İŞTE FARK BURADA:
    // API, kategoriyi bir obje olarak yollar.
    // Entity (Veritabanı) ise bunu parçalanmış stringler olarak tutar.

    @SerializedName("categoryId")
    val category: Category? = null,// Bazen null gelebilir, o yüzden ? koyduk

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?
)