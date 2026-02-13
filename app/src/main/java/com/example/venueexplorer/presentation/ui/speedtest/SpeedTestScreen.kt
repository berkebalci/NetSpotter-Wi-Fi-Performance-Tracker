package com.example.venueexplorer.presentation.ui.speedtest

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.venueexplorer.presentation.state.SpeedTestPhase
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Speed Test",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Phase Indicator
                PhaseIndicator(currentPhase = uiState.currentPhase)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Main Speed Display
                SpeedGauge(
                    currentPhase = uiState.currentPhase,
                    downloadSpeed = uiState.downloadSpeed,
                    uploadSpeed = uiState.uploadSpeed,
                    progress = uiState.progress,
                    isRunning = uiState.isTestRunning
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Results Cards
                ResultsSection(
                    ping = uiState.ping,
                    jitter = uiState.jitter,
                    downloadSpeed = uiState.downloadSpeed,
                    uploadSpeed = uiState.uploadSpeed
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Start/Stop Button
                SpeedTestButton(
                    isRunning = uiState.isTestRunning,
                    onStartClick = { viewModel.startSpeedTest() },
                    onStopClick = { viewModel.stopSpeedTest() }
                )
                
                // Error Display
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorCard(
                        error = uiState.error!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

@Composable
fun PhaseIndicator(currentPhase: SpeedTestPhase) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PhaseChip(
            label = "Ping",
            icon = Icons.Default.NetworkCheck,
            isActive = currentPhase == SpeedTestPhase.PING,
            isComplete = currentPhase.ordinal > SpeedTestPhase.PING.ordinal
        )
        PhaseChip(
            label = "Download",
            icon = Icons.Default.Download,
            isActive = currentPhase == SpeedTestPhase.DOWNLOAD,
            isComplete = currentPhase.ordinal > SpeedTestPhase.DOWNLOAD.ordinal
        )
        PhaseChip(
            label = "Upload",
            icon = Icons.Default.Upload,
            isActive = currentPhase == SpeedTestPhase.UPLOAD,
            isComplete = currentPhase == SpeedTestPhase.COMPLETE
        )
    }
}

@Composable
fun PhaseChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isComplete: Boolean
) {
    val backgroundColor = when {
        isComplete -> Color(0xFF4CAF50)
        isActive -> Color(0xFF2196F3)
        else -> Color.White
    }
    
    val contentColor = when {
        isComplete || isActive -> Color.White
        else -> Color(0xFF757575)
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isActive) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isComplete) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SpeedGauge(
    currentPhase: SpeedTestPhase,
    downloadSpeed: Double,
    uploadSpeed: Double,
    progress: Float,
    isRunning: Boolean
) {
    val currentSpeed = when (currentPhase) {
        SpeedTestPhase.DOWNLOAD -> downloadSpeed
        SpeedTestPhase.UPLOAD -> uploadSpeed
        else -> 0.0
    }
    
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = if (isRunning) progress else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )
    
    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Circle
        Canvas(modifier = Modifier.size(280.dp)) {
            drawCircle(
                color = Color(0xFFE0E0E0),
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Progress Arc
        Canvas(modifier = Modifier.size(280.dp)) {
            drawArc(
                color = Color(0xFF2196F3),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Speed Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", currentSpeed),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "Mbps",
                fontSize = 20.sp,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (currentPhase) {
                    SpeedTestPhase.IDLE -> "Ready"
                    SpeedTestPhase.PING -> "Testing Ping..."
                    SpeedTestPhase.DOWNLOAD -> "Downloading..."
                    SpeedTestPhase.UPLOAD -> "Uploading..."
                    SpeedTestPhase.COMPLETE -> "Complete!"
                },
                fontSize = 16.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ResultsSection(
    ping: Double,
    jitter: Double,
    downloadSpeed: Double,
    uploadSpeed: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultCard(
                modifier = Modifier.weight(1f),
                label = "Ping",
                value = "${ping.roundToInt()}",
                unit = "ms",
                icon = Icons.Default.Speed,
                color = Color(0xFFFF9800)
            )
            ResultCard(
                modifier = Modifier.weight(1f),
                label = "Jitter",
                value = "${jitter.roundToInt()}",
                unit = "ms",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF9C27B0)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultCard(
                modifier = Modifier.weight(1f),
                label = "Download",
                value = String.format("%.1f", downloadSpeed),
                unit = "Mbps",
                icon = Icons.Default.Download,
                color = Color(0xFF4CAF50)
            )
            ResultCard(
                modifier = Modifier.weight(1f),
                label = "Upload",
                value = String.format("%.1f", uploadSpeed),
                unit = "Mbps",
                icon = Icons.Default.Upload,
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = unit,
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SpeedTestButton(
    isRunning: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Button(
        onClick = { if (isRunning) onStopClick() else onStartClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRunning) Color(0xFFE53935) else Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isRunning) "Stop Test" else "Start Test",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
                Text(
                    text = error,
                    color = Color(0xFFE53935),
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}
