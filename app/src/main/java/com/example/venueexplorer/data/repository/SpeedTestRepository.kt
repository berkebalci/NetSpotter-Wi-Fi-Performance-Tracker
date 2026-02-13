package com.example.venueexplorer.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for internet speed testing operations.
 * Follows the repository pattern for separation of concerns.
 */
interface SpeedTestRepository {
    /**
     * Measures download speed by downloading test files.
     * @return Flow emitting real-time download speed in Mbps
     */
    suspend fun measureDownloadSpeed(): Flow<Double>
    
    /**
     * Measures upload speed by uploading test data.
     * @return Flow emitting real-time upload speed in Mbps
     */
    suspend fun measureUploadSpeed(): Flow<Double>
    
    /**
     * Measures ping latency to test servers.
     * @return Ping latency in milliseconds
     */
    suspend fun measurePing(): Double
    
    /**
     * Measures jitter (ping variation).
     * @return Jitter in milliseconds
     */
    suspend fun measureJitter(): Double
    
    /**
     * Cancels any ongoing speed test operations.
     */
    fun cancelTest()
}
