package com.example.shotly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.shotly.domain.model.ServiceState
import com.example.shotly.domain.repository.MediaProjectionRepository
import com.example.shotly.domain.repository.SettingsRepository
import com.example.shotly.ui.navigation.AppNavigation
import com.example.shotly.ui.theme.ShotlyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var mediaProjectionRepository: MediaProjectionRepository

    private var initialCheckDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("MainActivity created")

        // Habilitar edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ShotlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkCaptureOnOpen()
    }

    private fun checkCaptureOnOpen() {
        lifecycleScope.launch {
            val enabled = settingsRepository.captureOnAppOpen.first()
            val serviceState = mediaProjectionRepository.getServiceState().first()

            // Solo ejecutar si el servicio está activo, la opción está habilitada,
            // y no es un reinicio rápido de configuración (opcional, por ahora simple)
            if (enabled && serviceState is ServiceState.Active) {
                Timber.d("Capture on App Open triggered")
                // Minimizar Shotly para revelar la app anterior
                moveTaskToBack(true)
                
                // Trigger captura
                mediaProjectionRepository.captureScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity destroyed")
    }
}