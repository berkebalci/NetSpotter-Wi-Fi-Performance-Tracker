package com.example.venueexplorer.data.model

import com.google.gson.annotations.SerializedName

// Bu sınıf SADECE API'den gelen JSON verisini karşılamak içindir.
// Room veritabanı ile alakası yoktur.
data class Category(
    @SerializedName("_id") // MongoDB'de id alt çizgi ile gelir (_id)
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("iconName")
    val iconName: String
)