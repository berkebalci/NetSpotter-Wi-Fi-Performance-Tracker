package com.example.venueexplorer.presentation.state

/**
 * Represents the different phases of a speed test.
 */
enum class SpeedTestPhase {
    IDLE,
    PING,
    DOWNLOAD,
    UPLOAD,
    COMPLETE
}

/**
 * UI state for the Speed Test screen.
 */
data class SpeedTestUIState(
    val isTestRunning: Boolean = false,
    val currentPhase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val ping: Double = 0.0,
    val jitter: Double = 0.0,
    val progress: Float = 0f,
    val error: String? = null
)
