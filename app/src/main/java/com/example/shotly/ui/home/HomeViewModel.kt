package com.example.shotly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shotly.data.service.ScreenshotService
import com.example.shotly.domain.model.ServiceState
import com.example.shotly.domain.repository.MediaProjectionRepository
import com.example.shotly.domain.repository.SettingsRepository
import com.example.shotly.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaProjectionRepository: MediaProjectionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val serviceState: StateFlow<ServiceState> = mediaProjectionRepository.getServiceState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServiceState.Idle
        )

    init {
        // Collect settings and update UI state
        viewModelScope.launch {
            combine(
                settingsRepository.captureMode,
                settingsRepository.captureOnAppOpen
            ) { mode, onOpen ->
                Pair(mode, onOpen)
            }.collect { (mode, onOpen) ->
                _uiState.update { 
                    it.copy(
                        captureMode = mode,
                        captureOnAppOpen = onOpen
                    )
                }
            }
        }
    }

    fun requestPermission() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = mediaProjectionRepository.requestPermission()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            permissionIntent = result.data
                        )
                    }
                    Timber.d("Permission request prepared")
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                    Timber.e(result.exception, "Error requesting permission")
                }

                Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun startService(resultCode: Int, data: android.content.Intent) {
        viewModelScope.launch {
            val mode = settingsRepository.captureMode.first()
            when (val result = mediaProjectionRepository.startService(resultCode, data, mode)) {
                is Result.Success -> {
                    Timber.d("Service started successfully")
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message)
                    }
                    Timber.e(result.exception, "Error starting service")
                }

                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun captureScreen() {
        viewModelScope.launch {
            when (val result = mediaProjectionRepository.captureScreen()) {
                is Result.Success -> {
                    Timber.d("Screen capture requested: ${result.data}")
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message)
                    }
                    Timber.e(result.exception, "Error capturing screen")
                }

                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun stopService() {
        viewModelScope.launch {
            when (val result = mediaProjectionRepository.stopService()) {
                is Result.Success -> {
                    Timber.d("Service stopped successfully")
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message)
                    }
                    Timber.e(result.exception, "Error stopping service")
                }

                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun setCaptureMode(mode: Int) {
        viewModelScope.launch {
            settingsRepository.setCaptureMode(mode)
        }
    }

    fun setCaptureOnAppOpen(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCaptureOnAppOpen(enabled)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val permissionIntent: android.content.Intent? = null,
    val captureMode: Int = ScreenshotService.MODE_ACTIVE_SCREEN,
    val captureOnAppOpen: Boolean = false
)