package com.example.shotly.viewModel

import android.media.projection.MediaProjectionManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    // MediaProjectionManager será inicializado desde MainActivity
    lateinit var mpm: android.media.projection.MediaProjectionManager

    // Estado del servicio (true si está corriendo, false si no)
    private val _serviceRunning = MutableStateFlow(false)
    val serviceRunning: StateFlow<Boolean> = _serviceRunning

    fun setServiceRunning(running: Boolean) {
        _serviceRunning.value = running
    }

}
