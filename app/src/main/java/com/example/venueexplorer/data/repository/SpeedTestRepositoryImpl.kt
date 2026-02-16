package com.example.venueexplorer.data.repository

import android.util.Log
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
 * Implementation of SpeedTestRepository using OkHttpfor network operations.
 * Measures internet speed by downloading/uploading test files and calculating transfer rates.
 */
class SpeedTestRepositoryImpl(
    private val client: OkHttpClient
) : SpeedTestRepository {
    
    companion object {
        private const val TAG = "SpeedTestRepo"
    }
    
    // Test file URLs - using HTTP to avoid SSL certificate issues
    private val downloadTestUrls = listOf(
        "https://speed.cloudflare.com/__down?bytes=25000000", // 25 MB (İdeal Başlangıç)
        "https://speed.cloudflare.com/__down?bytes=50000000", // 50 MB (Yüksek Hızlar İçin)
    )

    // UPLOAD: Cloudflare Speedtest Upload Endpoint
// Bu adres hız testi uploadları için optimize edilmiştir, httpbin gibi banlamaz.
    private val uploadTestUrl = "https://speed.cloudflare.com/__up"

    // PING: Dünyanın en hızlı DNS sunucusu (Genelde en düşük latency buradadır)
    private val pingTestUrls = listOf(
        "https://1.1.1.1",
        "https://www.google.com"
    )
    
    @Volatile
    private var isCancelled = false
    
    override suspend fun measureDownloadSpeed(): Flow<Double> = flow {
        Log.d(TAG, "Starting download speed test, isCancelled=$isCancelled")
        
        // Use the 10MB file for download test
        val testUrl = downloadTestUrls[0]
        Log.d(TAG, "Download test URL: $testUrl")
        
        val request = Request.Builder()
            .url(testUrl)
            .build()
        
        try {
            Log.d(TAG, "Executing download request...")
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Response received - Success: ${response.isSuccessful}, Code: ${response.code}")
                
                if (!response.isSuccessful || isCancelled) {
                    Log.e(TAG, "Download failed or cancelled - Success: ${response.isSuccessful}, Cancelled: $isCancelled")
                    emit(0.0)
                    return@flow
                }
                
                val body = response.body ?: run {
                    Log.e(TAG, "Response body is null")
                    emit(0.0)
                    return@flow
                }
                
                val contentLength = body.contentLength()
                Log.d(TAG, "Content length: $contentLength bytes")
                
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
                            Log.d(TAG, "Download progress - Bytes: $totalBytesRead, Speed: $speedMbps Mbps")
                            emit(speedMbps)
                            lastEmitTime = currentTime
                        }
                    }
                }
                
                // Final speed calculation
                val totalElapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                if (totalElapsedSeconds > 0 && !isCancelled) {
                    val finalSpeedMbps = (totalBytesRead * 8.0) / (totalElapsedSeconds * 1_000_000)
                    Log.d(TAG, "Download complete - Total bytes: $totalBytesRead, Time: $totalElapsedSeconds s, Final speed: $finalSpeedMbps Mbps")
                    emit(finalSpeedMbps)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download test exception: ${e.message}", e)
            emit(0.0)
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun measureUploadSpeed(): Flow<Double> = flow {
        Log.d(TAG, "Starting upload speed test, isCancelled=$isCancelled")
        
        // Create 5MB of random data for upload test
        val uploadSize = 5 * 1024 * 1024 // 5MB
        val uploadData = ByteArray(uploadSize) { (it % 256).toByte() }
        Log.d(TAG, "Upload data size: $uploadSize bytes")
        
        val requestBody = uploadData.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url(uploadTestUrl)
            .post(requestBody)
            .build()
        
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d(TAG, "Executing upload request...")
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Upload response - Success: ${response.isSuccessful}, Code: ${response.code}")
                
                if (!response.isSuccessful || isCancelled) {
                    Log.e(TAG, "Upload failed or cancelled - Success: ${response.isSuccessful}, Cancelled: $isCancelled")
                    emit(0.0)
                    return@flow
                }
                
                val totalElapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                if (totalElapsedSeconds > 0 && !isCancelled) {
                    val speedMbps = (uploadSize * 8.0) / (totalElapsedSeconds * 1_000_000)
                    Log.d(TAG, "Upload complete - Size: $uploadSize bytes, Time: $totalElapsedSeconds s, Speed: $speedMbps Mbps")
                    emit(speedMbps)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload test exception: ${e.message}", e)
            emit(0.0)
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun measurePing(): Double = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting ping test, isCancelled=$isCancelled")
        if (isCancelled) {
            Log.d(TAG, "Ping test cancelled before start")
            return@withContext 0.0
        }
        
        val pingResults = mutableListOf<Double>()
        
        // Perform ping test to multiple servers
        pingTestUrls.forEach { url ->
            if (isCancelled) return@withContext 0.0
            
            try {
                Log.d(TAG, "Pinging: $url")
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
                        Log.d(TAG, "Ping to $url: $pingMs ms")
                    } else {
                        Log.w(TAG, "Ping to $url failed - Code: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ping to $url exception: ${e.message}")
            }
        }
        
        // Return average ping
        val avgPing = if (pingResults.isNotEmpty()) {
            pingResults.average()
        } else {
            0.0
        }
        Log.d(TAG, "Ping test complete - Average: $avgPing ms from ${pingResults.size} results")
        avgPing
    }
    
    override suspend fun measureJitter(): Double = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting jitter test, isCancelled=$isCancelled")
        if (isCancelled) {
            Log.d(TAG, "Jitter test cancelled before start")
            return@withContext 0.0
        }
        
        val pingResults = mutableListOf<Double>()
        
        // Perform multiple ping tests to calculate jitter
        repeat(5) { iteration ->
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
                        Log.d(TAG, "Jitter test iteration $iteration: $pingMs ms")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Jitter test iteration $iteration exception: ${e.message}")
            }
        }
        
        // Calculate jitter as standard deviation of ping times
        val jitter = if (pingResults.size >= 2) {
            val mean = pingResults.average()
            val variance = pingResults.map { (it - mean).pow(2) }.average()
            sqrt(variance)
        } else {
            0.0
        }
        Log.d(TAG, "Jitter test complete - Jitter: $jitter ms from ${pingResults.size} samples")
        jitter
    }
    
    override fun cancelTest() {
        Log.d(TAG, "Test cancelled")
        isCancelled = true
    }
    
    fun resetCancellation() {
        Log.d(TAG, "Resetting cancellation flag")
        isCancelled = false
    }
}
