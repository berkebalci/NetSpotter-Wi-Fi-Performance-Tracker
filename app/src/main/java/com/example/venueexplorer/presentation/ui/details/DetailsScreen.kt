package com.example.venueexplorer.presentation.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun DetailsScreen(
    venueId: String,
    viewModel: DetailsScreenViewModel,
    onBackClicked: () -> Unit,
    onEditButtonClicked: (venueId: String) -> Unit,
    onMapContainerClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Ekran açıldığında veriyi yükle
    LaunchedEffect(venueId) {
        viewModel.loadVenueDetails(venueId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when {
            // ═══════════════════════════════════════════════════════
            // LOADING STATE
            // ═══════════════════════════════════════════════════════
            uiState.isLoading -> {
                LoadingDetailsState()
            }

            // ═══════════════════════════════════════════════════════
            // ERROR STATE
            // ═══════════════════════════════════════════════════════
            uiState.isError -> {
                ErrorDetailsState(
                    errorMessage = uiState.errorMessage,
                    onRetry = {
                        viewModel.clearError()
                        viewModel.loadVenueDetails(venueId)
                    },
                    onBackClicked = onBackClicked
                )
            }

            // ═══════════════════════════════════════════════════════
            // EMPTY STATE
            // ═══════════════════════════════════════════════════════
            uiState.venue == null -> {
                EmptyDetailsState(onBackClicked = onBackClicked)
            }

            // ═══════════════════════════════════════════════════════
            // SUCCESS STATE - CONTENT
            // ═══════════════════════════════════════════════════════
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Navigation Bar
                    ModernDetailsTopBar(
                        onBackClicked = onBackClicked
                    )

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Hero Image Section
                        ModernHeroImage(
                            category = uiState.category
                        )

                        // Content Section
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Title
                            Text(
                                text = uiState.venue!!.title,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color(0xFF212121),
                                lineHeight = 38.sp
                            )

                            // Category & Info Row
                            ModernCategoryInfoRow(
                                category = uiState.category
                            )

                            // Rating Section
                            ModernRatingSection(
                                rating = uiState.venue!!.rating
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Notes/Description Section
                            ModernNotesSection(
                                description = uiState.venue!!.description
                            )

                            // Location Section (Placeholder)
                            ModernLocationSection(
                                latitude = uiState.venue!!.latitude,
                                longitude = uiState.venue!!.longitude,
                                onMapClick = onMapContainerClicked
                            )

                            // Bottom padding for fixed buttons
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }

                    // Fixed Action Buttons at Bottom
                    ModernActionButtons(
                        onEditClick = { onEditButtonClicked(venueId) },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        ModernDeleteDialog(
            venueName = uiState.venue?.title ?: "",
            onConfirm = {
                viewModel.deleteVenue(venueId) {
                    showDeleteDialog = false
                    onBackClicked()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

// ═══════════════════════════════════════════════════════
// COMPOSABLE COMPONENTS
// ═══════════════════════════════════════════════════════

@Composable
fun ModernDetailsTopBar(
    onBackClicked: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color(0xFFF5F5F5).copy(alpha = 0.95f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF212121)
                )
            }

            // Title (fades in)
            Text(
                text = "Venue Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )

            // Share Button
            IconButton(
                onClick = { /* Share action */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun ModernHeroImage(
    category: com.example.venueexplorer.data.local.CategoryEntity?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                category?.let {
                    Color(android.graphics.Color.parseColor(it.color)).copy(alpha = 0.3f)
                } ?: Color(0xFFE0E0E0)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
        )

        // Category Icon
        Icon(
            imageVector = category?.let { getCategoryIcon(it.iconName) } ?: Icons.Default.Place,
            contentDescription = null,
            tint = category?.let {
                Color(android.graphics.Color.parseColor(it.color))
            } ?: Color(0xFF757575),
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
fun ModernCategoryInfoRow(
    category: com.example.venueexplorer.data.local.CategoryEntity?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Chip
        if (category != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.15f),
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconName),
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(category.color)),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = category.name,
                        color = Color(android.graphics.Color.parseColor(category.color)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Additional Info
        Text(
            text = "• $$$ • 1.2 mi away",
            color = Color(0xFF757575),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun ModernRatingSection(
    rating: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star Rating Display
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (index < rating.toInt()) Color(0xFFFFA000) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = "Based on your visit",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Normal
                )
            }

            // Rating Number
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.1f", rating),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = getRatingLabel(rating),
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
fun ModernNotesSection(
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Notes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = description,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.Normal
                )

                // Metadata
                HorizontalDivider(color = Color(0xFFE0E0E0))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Visited Oct 12, 2023",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Dinner",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernLocationSection(
    latitude: Double?,
    longitude: Double?,
    onMapClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Location",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        // Map Placeholder (Clickable)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable(onClick = onMapClick),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE3F2FD)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    if (latitude != null && longitude != null) {
                        Text(
                            text = "Tap to view on map",
                            fontSize = 14.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Location data not available",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }

        // Address
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF757575),
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Text(
                text = if (latitude != null && longitude != null) {
                    "$latitude, $longitude"
                } else {
                    "Address not available"
                },
                fontSize = 14.sp,
                color = Color(0xFF757575),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ModernActionButtons(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF5F5F5).copy(alpha = 0.98f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit Button (Outlined)
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF2196F3)))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2196F3)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Delete Button (Filled Red)
            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ModernDeleteDialog(
    venueName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Venue",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete \"$venueName\"?",
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
                Text(
                    text = "This action cannot be undone.",
                    fontSize = 14.sp,
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = Color(0xFF757575))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// ═══════════════════════════════════════════════════════
// STATE COMPOSABLES
// ═══════════════════════════════════════════════════════

@Composable
fun LoadingDetailsState() {
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
                text = "Loading venue details...",
                color = Color(0xFF757575),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ErrorDetailsState(
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = errorMessage ?: "Something went wrong",
                color = Color(0xFF424242),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
            OutlinedButton(
                onClick = onBackClicked,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Go Back")
            }
        }
    }
}

@Composable
fun EmptyDetailsState(
    onBackClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Venue not found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            Text(
                text = "This venue may have been deleted",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Button(
                onClick = onBackClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Go Back")
            }
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

fun getRatingLabel(rating: Float): String {
    return when {
        rating >= 4.5 -> "Excellent"
        rating >= 4.0 -> "Very Good"
        rating >= 3.5 -> "Good"
        rating >= 3.0 -> "Average"
        else -> "Below Average"
    }
}