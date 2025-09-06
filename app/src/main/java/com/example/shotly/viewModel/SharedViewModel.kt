package com.example.shotly.viewModel

import android.media.projection.MediaProjectionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shotly.ScreenshotService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SharedViewModel : ViewModel() {
    lateinit var mpm: MediaProjectionManager

    // Expone el estado del servicio a la UI de forma segura para el ciclo de vida
    val isServiceActive = ScreenshotService.isServiceActive.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
}
