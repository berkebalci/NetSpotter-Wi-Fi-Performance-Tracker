// presentation/state/EditScreenUIState.kt
package com.example.venueexplorer.presentation.state

import com.example.venueexplorer.data.local.CategoryEntity

data class EditScreenUIState(
    val title: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val selectedCategoryId: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false, // Yeni alan: Edit mi yoksa Add mi?
    val venueId: String? = null, // Edit modunda venue ID'si,
    val latitude: Double? = null,
    val longitude: Double? = null
)