package com.example.venueexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    // Spinner'ı doldurmak için hepsini getir
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    // API'den gelenleri topluca kaydet
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    // Temizlik için
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}