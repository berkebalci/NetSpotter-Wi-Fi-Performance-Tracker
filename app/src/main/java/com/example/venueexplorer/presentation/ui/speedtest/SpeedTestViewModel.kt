package com.example.venueexplorer.presentation.ui.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.venueexplorer.data.repository.SpeedTestRepository
import com.example.venueexplorer.presentation.state.SpeedTestPhase
import com.example.venueexplorer.presentation.state.SpeedTestUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Speed Test screen.
 * Manages speed test execution and UI state updates.
 */
class SpeedTestViewModel(
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SpeedTestUIState())
    val uiState: StateFlow<SpeedTestUIState> = _uiState.asStateFlow()
    
    private var testJob: Job? = null
    
    /**
     * Starts a complete speed test sequence: Ping → Download → Upload
     */
    fun startSpeedTest() {
        // Cancel any existing test
        stopSpeedTest()
        
        testJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isTestRunning = true,
                        currentPhase = SpeedTestPhase.PING,
                        progress = 0f,
                        error = null,
                        downloadSpeed = 0.0,
                        uploadSpeed = 0.0,
                        ping = 0.0,
                        jitter = 0.0
                    )
                }
                
                // Phase 1: Measure Ping
                val ping = speedTestRepository.measurePing()
                _uiState.update {
                    it.copy(
                        ping = ping,
                        progress = 0.25f
                    )
                }
                
                // Phase 2: Measure Jitter
                val jitter = speedTestRepository.measureJitter()
                _uiState.update {
                    it.copy(
                        jitter = jitter,
                        progress = 0.33f,
                        currentPhase = SpeedTestPhase.DOWNLOAD
                    )
                }
                
                // Phase 3: Measure Download Speed
                speedTestRepository.measureDownloadSpeed()
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                error = "Download test failed: ${e.message}",
                                isTestRunning = false
                            )
                        }
                    }
                    .collect { speed ->
                        _uiState.update {
                            it.copy(
                                downloadSpeed = speed,
                                progress = 0.33f + (0.33f * 0.8f) // Progress through download phase
                            )
                        }
                    }
                
                _uiState.update {
                    it.copy(
                        progress = 0.66f,
                        currentPhase = SpeedTestPhase.UPLOAD
                    )
                }
                
                // Phase 4: Measure Upload Speed
                speedTestRepository.measureUploadSpeed()
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                error = "Upload test failed: ${e.message}",
                                isTestRunning = false
                            )
                        }
                    }
                    .collect { speed ->
                        _uiState.update {
                            it.copy(
                                uploadSpeed = speed,
                                progress = 0.66f + (0.34f * 0.8f) // Progress through upload phase
                            )
                        }
                    }
                
                // Test Complete
                _uiState.update {
                    it.copy(
                        currentPhase = SpeedTestPhase.COMPLETE,
                        progress = 1f,
                        isTestRunning = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Speed test failed: ${e.message}",
                        isTestRunning = false,
                        currentPhase = SpeedTestPhase.IDLE
                    )
                }
            }
        }
    }
    
    /**
     * Stops the currently running speed test.
     */
    fun stopSpeedTest() {
        testJob?.cancel()
        speedTestRepository.cancelTest()
        _uiState.update {
            it.copy(
                isTestRunning = false,
                currentPhase = SpeedTestPhase.IDLE
            )
        }
    }
    
    /**
     * Clears any error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopSpeedTest()
    }
}
