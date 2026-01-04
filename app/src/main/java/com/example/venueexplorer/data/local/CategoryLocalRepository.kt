package com.example.venueexplorer.data.local

import android.util.Log
import com.example.venueexplorer.data.model.Category
import com.example.venueexplorer.data.remote.VenueApiService


class CategoryLocalRepository(
    private val api: VenueApiService,
    private val categoryDao: CategoryDao
) {
    private val TAG = "TAGCategoryRepo"
    suspend fun getAllCategories(): List<CategoryEntity>{
        try {
            val categories = api.getCategories()
            val entities = categories.map { it.toEntity() }
            deleteAll()
            categoryDao.insertAll(entities)
        }
        catch (e: Exception){
            Log.e(TAG, "Hata: ${e.message}")
            throw e
        }
        return categoryDao.getAllCategories()


    }

    suspend fun deleteAll() {
        categoryDao.deleteAll()
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            color = color,
            iconName = iconName)
    }
}
