package com.example.venueexplorer.data.model

import com.google.gson.annotations.SerializedName

data class VenueRequest (
    
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
    val categoryId: String?
)
