package com.example.venueexplorer.data.model

data class SpeedTestResult(
    val downloadSpeedMbps: Double = 0.0,
    val uploadSpeedMbps: Double = 0.0,
    val pingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
