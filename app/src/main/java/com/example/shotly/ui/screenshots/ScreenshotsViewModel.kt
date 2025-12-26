package com.example.shotly.ui.screenshots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shotly.domain.model.Screenshot
import com.example.shotly.domain.repository.ScreenshotRepository
import com.example.shotly.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScreenshotsViewModel @Inject constructor(
    private val screenshotRepository: ScreenshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotsUiState())
    val uiState: StateFlow<ScreenshotsUiState> = _uiState.asStateFlow()

    fun loadScreenshots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            screenshotRepository.getAllScreenshots()
                .catch { exception ->
                    Timber.e(exception, "Error loading screenshots")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error desconocido"
                        )
                    }
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    screenshots = result.data,
                                    errorMessage = null
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = result.exception.message ?: "Error desconocido"
                                )
                            }
                        }
                        Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
        }
    }

    fun deleteScreenshot(id: Long) {
        viewModelScope.launch {
            when (val result = screenshotRepository.deleteScreenshot(id)) {
                is Result.Success -> {
                    Timber.d("Screenshot deleted successfully: $id")
                    // La lista se actualizará automáticamente via Flow
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.exception.message ?: "Error eliminando captura")
                    }
                    Timber.e(result.exception, "Error deleting screenshot: $id")
                }
                Result.Loading -> {
                    // No necesario para esta operación
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class ScreenshotsUiState(
    val isLoading: Boolean = false,
    val screenshots: List<Screenshot> = emptyList(),
    val errorMessage: String? = null
)