package com.example.shotly.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.shotly.ScreenshotService
import com.example.shotly.ScreenshotService.Companion.isServiceActive
import com.example.shotly.viewModel.SharedViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun HomeScreen(sharedVM: SharedViewModel) {
    val context = LocalContext.current

    val isServiceActive by sharedVM.isServiceActive.collectAsState()

    var hasNotifPerm by remember {
        mutableStateOf(checkNotificationsPermission(context))
    }

    var serviceRunning by remember { mutableStateOf(false) }
    val mpm = sharedVM.mpm
    val mpLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val startIntent = Intent(context, ScreenshotService::class.java).apply {
                action = ScreenshotService.ACTION_START
                putExtra(ScreenshotService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenshotService.EXTRA_DATA_INTENT, result.data)
            }
            ContextCompat.startForegroundService(context, startIntent)

            // Después de un pequeño delay, disparar captura
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                val captureIntent = Intent(context, ScreenshotService::class.java).apply {
                    action = ScreenshotService.ACTION_CAPTURE
                }
                ContextCompat.startForegroundService(context, captureIntent)
            }, 500)
        }

    }

    // Launcher para el permiso de MediaProjection
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val startIntent = Intent(context, ScreenshotService::class.java).apply {
                action = ScreenshotService.ACTION_START
                putExtra(ScreenshotService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenshotService.EXTRA_DATA_INTENT, result.data)
            }
            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    // Launcher para el permiso de notificaciones (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted
        // Una vez gestionado el permiso de notificación, pedir el de captura si es necesario
        if (granted && !isServiceActive) {
            mediaProjectionLauncher.launch(sharedVM.mpm.createScreenCaptureIntent())
        }
    }

    // Función para iniciar el proceso de permisos y servicio
    fun startCaptureProcess() {
        val isTiramisuOrUp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        if (isTiramisuOrUp && !hasNotifPerm) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            mediaProjectionLauncher.launch(sharedVM.mpm.createScreenCaptureIntent())
        }
    }
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPerm) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            mpLauncher.launch(mpm.createScreenCaptureIntent())
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ScreenShots", style = MaterialTheme.typography.headlineMedium)
            Text(if (!serviceRunning) "Iniciar servicio (pedir permiso)" else "Reiniciar servicio")
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
            )
            Text("Shotly Screen Recorder", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))


            // ✅ CORRECCIÓN: Usar el valor boolean directamente
            if (isServiceActive) {
                Button(onClick = {
                    val captureIntent = Intent(context, ScreenshotService::class.java).apply {
                        action = ScreenshotService.ACTION_CAPTURE
                    }
                    ContextCompat.startForegroundService(context, captureIntent)
                }) {
                    Text("Tomar Captura")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val stopIntent = Intent(context, ScreenshotService::class.java).apply {
                        action = ScreenshotService.ACTION_STOP
                    }
                    context.startService(stopIntent)
                }) {
                    Text("Detener Servicio")
                }
            } else {
                Button(onClick = { startCaptureProcess() }) {
                    Text("Iniciar Servicio")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estado para debug
            Text(
                text = "Servicio: ${if (isServiceActive) "Activo" else "Inactivo"}",
                style = MaterialTheme.typography.bodyMedium
            )

        }
    }

}

private fun checkNotificationsPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}
