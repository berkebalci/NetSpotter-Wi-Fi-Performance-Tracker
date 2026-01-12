package com.example.venueexplorer.presentation.ui.edit

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.jar.Manifest

@Composable
fun EditScreen(
    venueId: String?,
    viewModel: EditScreenViewModel,
    onSaveButtonClicked: () -> Unit,
    onCancelButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.updateLocationPermssion(true)
            }
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.updateLocationPermssion(true)
            }
            else -> {
                viewModel.updateLocationPermssion(false)
            }
        }
    }

    // Kayıt başarılı olduğunda callback çağır
    LaunchedEffect(venueId) {
        venueId?.let { id ->
            viewModel.loadVenueForEditing(id)
        }
    }
    //isEditMode
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveButtonClicked()
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7F8))
    ) {
        when {
            // ═══════════════════════════════════════════════════════
            // LOADING STATE
            // ═══════════════════════════════════════════════════════
            uiState.isLoading -> {
                ModernLoadingState()
            }

            // ═══════════════════════════════════════════════════════
            // FORM
            // ═══════════════════════════════════════════════════════
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top App Bar
                    ModernEditTopBar(
                        title = if (uiState.isEditMode) "Edit Venue" else "Add New Venue",
                        onCancelClick = onCancelButtonClicked,
                        isSaving = uiState.isSaving
                    )

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Error Message
                        if (uiState.isError) {
                            ModernErrorCard(
                                errorMessage = uiState.errorMessage ?: "Unknown error",
                                onDismiss = { viewModel.clearError() }
                            )
                        }

                        // Photo Upload Placeholder
                        ModernPhotoPlaceholder()

                        // Venue Name Input
                        ModernVenueNameInput(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            enabled = !uiState.isSaving
                        )

                        // Category Selection
                        ModernCategorySelection(
                            categories = uiState.categories,
                            selectedCategoryId = uiState.selectedCategoryId,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            enabled = !uiState.isSaving
                        )

                        // Rating Section
                        ModernRatingCard(
                            rating = uiState.rating,
                            onRatingChange = { viewModel.updateRating(it) },
                            enabled = !uiState.isSaving
                        )

                        // Description Input
                        ModernDescriptionInput(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            enabled = !uiState.isSaving
                        )

                        // Location Info (Placeholder)
                        ModernLocationCard(
                            onClick = {
                                locationPermissionRequest.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )
                    }

                    // Fixed Save Button
                    ModernSaveButton(
                        isEditMode = uiState.isEditMode,
                        isSaving = uiState.isSaving,
                        isEnabled = !uiState.isSaving && uiState.categories.isNotEmpty(),
                        onClick = {
                            viewModel.saveVenue()
                            if(uiState.isEditMode) onSaveButtonClicked()
                    }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// COMPOSABLE COMPONENTS
// ═══════════════════════════════════════════════════════

@Composable
fun ModernEditTopBar(
    title: String,
    onCancelClick: () -> Unit,
    isSaving: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color(0xFFF6F7F8).copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel Button
            TextButton(
                onClick = onCancelClick,
                enabled = !isSaving
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF2196F3),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF212121)
            )

            // Empty spacer for centering
            Spacer(modifier = Modifier.width(60.dp))
        }
    }
}

@Composable
fun ModernPhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE3E8EF))
            .clickable { /* Photo picker action */ },
        contentAlignment = Alignment.Center
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2196F3).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add Photo",
                tint = Color(0xFF8B95A5),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "This Section is not ready yet",
                color = Color(0xFF8B95A5),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernVenueNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Venue Name",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "e.g. The Coffee Spot",
                            color = Color(0xFFBDBDBD)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    enabled = enabled,
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ModernCategorySelection(
    categories: List<com.example.venueexplorer.data.local.CategoryEntity>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(start = 4.dp)
        )

        if (categories.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE53935)
                    )
                    Text(
                        text = "No categories available",
                        color = Color(0xFFE53935),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(categories) { category ->
                    ModernCategoryChip(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
fun ModernCategoryChip(
    category: com.example.venueexplorer.data.local.CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF2196F3) else Color.White,
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = getCategoryIcon(category.iconName),
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF757575),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = category.name,
                color = if (isSelected) Color.White else Color(0xFF424242),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ModernRatingCard(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Rating",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2196F3).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = String.format("%.1f", rating),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Star Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = {
                            if (enabled) {
                                onRatingChange((index + 1).toFloat())
                            }
                        },
                        enabled = enabled,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (index < rating.toInt()) Color(0xFFFFA000) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(rating / 5f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2196F3))
                )
            }

            // Hint Text
            Text(
                text = "Tap stars to rate",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ModernDescriptionInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Description & Notes",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                placeholder = {
                    Text(
                        text = "What did you like about this place? Any tips?",
                        color = Color(0xFFBDBDBD),
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp
                )
            )
        }
    }
}

@Composable
fun ModernLocationCard(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,

            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "This Section is not available yet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "It will be ready :)",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun ModernSaveButton(
    isEditMode: Boolean,
    isSaving: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF6F7F8).copy(alpha = 0.98f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                enabled = isEnabled
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEditMode) "Update Venue" else "Save Venue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ModernErrorCard(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFEBEE),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = errorMessage,
                modifier = Modifier.weight(1f),
                color = Color(0xFFE53935),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ModernLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2196F3),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading...",
                color = Color(0xFF757575),
                fontSize = 16.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "coffee" -> Icons.Outlined.Coffee
        "restaurant" -> Icons.Outlined.Restaurant
        "museum" -> Icons.Outlined.Museum
        "park" -> Icons.Outlined.Park
        else -> Icons.Default.Place
    }
}