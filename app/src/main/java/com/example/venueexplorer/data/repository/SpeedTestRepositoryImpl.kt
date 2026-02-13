package com.example.venueexplorer.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Implementation of SpeedTestRepository using OkHttp for network operations.
 * Measures internet speed by downloading/uploading test files and calculating transfer rates.
 */
class SpeedTestRepositoryImpl : SpeedTestRepository {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Test file URLs - using publicly available files from CDNs
    private val downloadTestUrls = listOf(
        "https://speed.hetzner.de/1GB.bin",  // 1GB file
        "https://speed.hetzner.de/100MB.bin", // 100MB file
        "https://speed.hetzner.de/10MB.bin"   // 10MB file
    )
    
    // Upload test endpoint (we'll use a POST request to measure upload speed)
    private val uploadTestUrl = "https://httpbin.org/post"
    
    // Ping test servers
    private val pingTestUrls = listOf(
        "https://www.google.com",
        "https://www.cloudflare.com",
        "https://www.amazon.com"
    )
    
    @Volatile
    private var isCancelled = false
    
    override suspend fun measureDownloadSpeed(): Flow<Double> = flow {
        isCancelled = false
        
        // Use the 10MB file for download test to get quick results
        val testUrl = downloadTestUrls[2]
        
        val request = Request.Builder()
            .url(testUrl)
            .build()
        
        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful || isCancelled) {
                    emit(0.0)
                    return@withContext
                }
                
                val body = response.body ?: run {
                    emit(0.0)
                    return@withContext
                }
                
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                val buffer = ByteArray(8192)
                
                var totalBytesRead = 0L
                val startTime = System.currentTimeMillis()
                var lastEmitTime = startTime
                
                while (!isCancelled) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) break
                    
                    totalBytesRead += bytesRead
                    
                    // Emit speed update every 200ms
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastEmitTime >= 200) {
                        val elapsedSeconds = (currentTime - startTime) / 1000.0
                        if (elapsedSeconds > 0) {
                            val speedMbps = (totalBytesRead * 8.0) / (elapsedSeconds * 1_000_000)
                            emit(speedMbps)
                            lastEmitTime = currentTime
                        }
                    }
                }
                
                // Final speed calculation
                val totalElapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                if (totalElapsedSeconds > 0 && !isCancelled) {
                    val finalSpeedMbps = (totalBytesRead * 8.0) / (totalElapsedSeconds * 1_000_000)
                    emit(finalSpeedMbps)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun measureUploadSpeed(): Flow<Double> = flow {
        isCancelled = false
        
        // Create 5MB of random data for upload test
        val uploadSize = 5 * 1024 * 1024 // 5MB
        val uploadData = ByteArray(uploadSize) { (it % 256).toByte() }
        
        val requestBody = uploadData.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url(uploadTestUrl)
            .post(requestBody)
            .build()
        
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful || isCancelled) {
                        emit(0.0)
                        return@withContext
                    }
                    
                    val totalElapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                    if (totalElapsedSeconds > 0 && !isCancelled) {
                        val speedMbps = (uploadSize * 8.0) / (totalElapsedSeconds * 1_000_000)
                        emit(speedMbps)
                    }
                }
            } catch (e: Exception) {
                if (!isCancelled) {
                    emit(0.0)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun measurePing(): Double = withContext(Dispatchers.IO) {
        if (isCancelled) return@withContext 0.0
        
        val pingResults = mutableListOf<Double>()
        
        // Perform ping test to multiple servers
        pingTestUrls.forEach { url ->
            if (isCancelled) return@withContext 0.0
            
            try {
                val request = Request.Builder()
                    .url(url)
                    .head() // Use HEAD request for minimal data transfer
                    .build()
                
                val startTime = System.nanoTime()
                client.newCall(request).execute().use { response ->
                    val endTime = System.nanoTime()
                    
                    if (response.isSuccessful) {
                        val pingMs = (endTime - startTime) / 1_000_000.0
                        pingResults.add(pingMs)
                    }
                }
            } catch (e: Exception) {
                // Skip failed pings
            }
        }
        
        // Return average ping
        if (pingResults.isNotEmpty()) {
            pingResults.average()
        } else {
            0.0
        }
    }
    
    override suspend fun measureJitter(): Double = withContext(Dispatchers.IO) {
        if (isCancelled) return@withContext 0.0
        
        val pingResults = mutableListOf<Double>()
        
        // Perform multiple ping tests to calculate jitter
        repeat(5) {
            if (isCancelled) return@withContext 0.0
            
            try {
                val request = Request.Builder()
                    .url(pingTestUrls[0])
                    .head()
                    .build()
                
                val startTime = System.nanoTime()
                client.newCall(request).execute().use { response ->
                    val endTime = System.nanoTime()
                    
                    if (response.isSuccessful) {
                        val pingMs = (endTime - startTime) / 1_000_000.0
                        pingResults.add(pingMs)
                    }
                }
            } catch (e: Exception) {
                // Skip failed pings
            }
        }
        
        // Calculate jitter as standard deviation of ping times
        if (pingResults.size >= 2) {
            val mean = pingResults.average()
            val variance = pingResults.map { (it - mean).pow(2) }.average()
            sqrt(variance)
        } else {
            0.0
        }
    }
    
    override fun cancelTest() {
        isCancelled = true
    }
}
