package com.example.venueexplorer.data.local


interface CategoryLocalRepository {
    suspend fun getAllCategories(): List<CategoryEntity>
    suspend fun insertAllCategories(categories: List<CategoryEntity>)
    suspend fun deleteAll()
}
