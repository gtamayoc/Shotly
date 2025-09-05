package com.example.shotly.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.shotly.ScreenshotService
import com.example.shotly.viewModel.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(sharedVM: SharedViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasNotifPerm by remember { mutableStateOf(checkNotificationsPermission(context)) }
    val serviceRunning by sharedVM.serviceRunning.collectAsState() // stateflow en ViewModel

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

            scope.launch {
                delay(1000) // coroutine en vez de Handler
                val captureIntent = Intent(context, ScreenshotService::class.java).apply {
                    action = ScreenshotService.ACTION_CAPTURE
                }
                ContextCompat.startForegroundService(context, captureIntent)
            }

            sharedVM.setServiceRunning(true)
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPerm = granted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        if (hasNotifPerm) {
            mpLauncher.launch(mpm.createScreenCaptureIntent())
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPerm) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            mpLauncher.launch(mpm.createScreenCaptureIntent())
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Shotly - Capturas", style = MaterialTheme.typography.headlineMedium)
            Text(if (!serviceRunning) "Servicio detenido" else "Servicio en ejecución")
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
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