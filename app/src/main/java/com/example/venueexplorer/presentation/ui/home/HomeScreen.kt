package com.example.venueexplorer.presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.venueexplorer.data.local.CategoryEntity
import com.example.venueexplorer.data.local.VenueEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onNavigateToEditScreen: (String?) -> Unit,
    onNavigateToDetailsScreen: (String) -> Unit,
    onNavigateToAddVenue: () -> Unit,
    onNavigateToSpeedTest: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section
            ModernHeader(onNavigateToSpeedTest = onNavigateToSpeedTest)

            // Search Bar
            ModernSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.searchVenues(it) }
            )

            // Category Filter Chips
            if (uiState.categories.isNotEmpty()) {
                ModernCategoryFilter(
                    categories = uiState.categories,
                    onCategorySelected = { viewModel.filterByCategory(it) }
                )
            }

            // Venue List
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = {
                    viewModel.refreshData()
                },
                )
            {
                Box {
                    when {
                        uiState.isLoading -> {
                            LoadingState()
                        }

                        uiState.isError -> {
                            ErrorState(
                                errorMessage = uiState.errorMessage,
                                onRetry = {
                                    viewModel.clearError()
                                    viewModel.refreshData()
                                }
                            )
                        }

                        uiState.venues.isEmpty() -> {
                            EmptyState(
                                searchQuery = uiState.searchQuery,
                                onAddClick = { onNavigateToEditScreen(null) }
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = uiState.venues,
                                    key = { it.id }
                                ) { venue ->
                                    val category = uiState.categories.find {
                                        it.id == venue.categoryId
                                    }
                                    Log.e("HomeScreen", "VenueId ${venue.id}")

                                    ModernVenueCard(
                                        venue = venue,
                                        category = category,
                                        onClick = { onNavigateToDetailsScreen(venue.id) },
                                        onDelete = { viewModel.deleteVenue(venue.id) }
                                    )
                                }

                                // Bottom padding for FAB
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { onNavigateToAddVenue() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF2196F3),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Venue",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ModernHeader(onNavigateToSpeedTest: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 50.dp)
    ) {
        Text(
            text = "WELCOME BACK",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF9E9E9E),
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Venue Explorer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFF212121)
            )

            // Speed Test Icon
            IconButton(
                onClick = onNavigateToSpeedTest,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Speed Test",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
            // Profile Icon
            /*Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD7B899)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }*/
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(24.dp)
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Find a venue, coffee, gym...",
                        color = Color(0xFFBDBDBD),
                        fontSize = 15.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Filter",
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ModernCategoryFilter(
    categories: List<CategoryEntity>,
    onCategorySelected: (String?) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "All" chip
        item {
            ModernCategoryChip(
                label = "All",
                icon = Icons.Default.Apps,
                isSelected = selectedCategoryId == null,
                color = Color(0xFF212121),
                onClick = {
                    selectedCategoryId = null
                    onCategorySelected(null)
                }
            )
        }

        // Category chips
        items(categories) { category ->
            ModernCategoryChip(
                label = category.name,
                icon = getCategoryIcon(category.iconName),
                isSelected = selectedCategoryId == category.id,
                color = Color(android.graphics.Color.parseColor(category.color)),
                onClick = {
                    selectedCategoryId = category.id
                    onCategorySelected(category.id)
                }
            )
        }
    }
}

@Composable
fun ModernCategoryChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) color else Color.White,
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF424242),
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ModernVenueCard(
    venue: VenueEntity,
    category: CategoryEntity?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Venue Image Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        category?.let {
                            Color(android.graphics.Color.parseColor(it.color)).copy(alpha = 0.2f)
                        } ?: Color(0xFFE0E0E0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category?.let { getCategoryIcon(it.iconName) }
                        ?: Icons.Default.Place,
                    contentDescription = null,
                    tint = category?.let {
                        Color(android.graphics.Color.parseColor(it.color))
                    } ?: Color(0xFF757575),
                    modifier = Modifier.size(48.dp)
                )
            }

            // Venue Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = venue.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = venue.description.take(30) + if (venue.description.length > 30) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rating Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", venue.rating),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF212121)
                        )

                        // Star Icons
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < venue.rating.toInt()) Color(0xFFFFA000) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Category Icon Badge
            if (category != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.15f)
                        )
                        .clickable { showDeleteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconName),
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(category.color)),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Venue",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete ${venue.title}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun LoadingState() {
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
                text = "Loading venues...",
                color = Color(0xFF757575),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ErrorState(
    errorMessage: String?,
    onRetry: () -> Unit
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
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyState(
    searchQuery: String,
    onAddClick: () -> Unit
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
                text = if (searchQuery.isNotBlank()) {
                    "No results for \"$searchQuery\""
                } else {
                    "No venues yet"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Try a different search"
                } else {
                    "Add your first venue to get started"
                },
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            if (searchQuery.isBlank()) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add First Venue")
                }
            }
        }
    }
}

// Helper function to get category icons
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "coffee" -> Icons.Outlined.Coffee
        "restaurant" -> Icons.Outlined.Restaurant
        "museum" -> Icons.Outlined.Museum
        "park" -> Icons.Outlined.Park
        else -> Icons.Default.Place
    }
}