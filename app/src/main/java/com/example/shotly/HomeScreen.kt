package com.example.shotly

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun HomeScreen(mpm: MediaProjectionManager) {
    val context = LocalContext.current
    var hasNotifPerm by remember { mutableStateOf(checkNotificationsPermission(context)) }
    var serviceRunning by remember { mutableStateOf(false) }

    // Launcher para MediaProjection
    val mpLauncher = remember {
        (context as? ComponentActivity)?.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val startIntent = Intent(context, ScreenshotService::class.java).apply {
                    action = ScreenshotService.ACTION_START
                    putExtra(ScreenshotService.EXTRA_RESULT_CODE, result.resultCode)
                    putExtra(ScreenshotService.EXTRA_DATA_INTENT, result.data)
                }
                ContextCompat.startForegroundService(context, startIntent)
                serviceRunning = true
            }
        }
    }

    // Launcher POST_NOTIFICATIONS (Android 13+)
    val notifPermLauncher = remember {
        (context as? ComponentActivity)?.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasNotifPerm = granted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Capturas de pantalla con ForegroundService",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPerm) {
                    notifPermLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    return@Button
                }
                mpLauncher?.launch(mpm.createScreenCaptureIntent())
            }
        ) {
            Text(if (!serviceRunning) "Iniciar servicio (pedir permiso)" else "Reiniciar servicio")
        }

        Button(
            enabled = serviceRunning,
            onClick = {
                val captureIntent = Intent(context, ScreenshotService::class.java).apply {
                    action = ScreenshotService.ACTION_CAPTURE
                }
                ContextCompat.startForegroundService(context, captureIntent)
            }
        ) {
            Text("Tomar captura ahora (desde app)")
        }

        Button(
            enabled = serviceRunning,
            onClick = {
                val stopIntent = Intent(context, ScreenshotService::class.java).apply {
                    action = ScreenshotService.ACTION_STOP
                }
                ContextCompat.startForegroundService(context, stopIntent)
                serviceRunning = false
                // Opcional: limpiar notificaciones
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .cancel(ScreenshotService.NOTIF_ID)
            }
        ) {
            Text("Detener servicio")
        }

        Text(
            "Una vez iniciado, verás una notificación persistente con una acción para capturar. " +
                    "Puedes seguir usando el botón de la notificación para tomar capturas en cualquier momento."
        )
    }
}

private fun checkNotificationsPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
