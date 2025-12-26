package com.example.shotly.ui.home

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shotly.data.service.ScreenshotService
import com.example.shotly.domain.model.ServiceState
import com.example.shotly.ui.components.ErrorDialog
import com.example.shotly.ui.components.LoadingIndicator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val serviceState by viewModel.serviceState.collectAsStateWithLifecycle()

    // Permission for notifications (Android 13+)
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // Launcher for MediaProjection permission
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startService(result.resultCode, result.data!!)
        }
    }

    // Handle permission intent
    LaunchedEffect(uiState.permissionIntent) {
        uiState.permissionIntent?.let { intent ->
            mediaProjectionLauncher.launch(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // Title and description
        Text(
            text = "Shotly",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Captura de pantalla profesional",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Service status card
        ServiceStatusCard(serviceState = serviceState)

        // Settings Section
        SettingsCard(
            captureMode = uiState.captureMode,
            captureOnOpen = uiState.captureOnAppOpen,
            onModeChanged = viewModel::setCaptureMode,
            onToggleOnOpen = viewModel::setCaptureOnAppOpen,
            enabled = serviceState is ServiceState.Idle // Solo editable si inactivo
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        when (serviceState) {
            is ServiceState.Idle -> {
                StartServiceButton(
                    onClick = {
                        if (notificationPermission?.status?.isGranted != false) {
                            viewModel.requestPermission()
                        } else {
                            notificationPermission.launchPermissionRequest()
                        }
                    },
                    isLoading = uiState.isLoading
                )
            }
            is ServiceState.Active -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CaptureButton(
                        onClick = { 
                            // Minimize logic if "Active Screen" mode
                            if (uiState.captureMode == ScreenshotService.MODE_ACTIVE_SCREEN) {
                                activity?.moveTaskToBack(true)
                            }
                            viewModel.captureScreen() 
                        }
                    )

                    OutlinedButton(
                        onClick = { viewModel.stopService() }
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detener Servicio")
                    }
                }
            }
            is ServiceState.Starting, is ServiceState.Capturing, is ServiceState.Stopping -> {
                LoadingIndicator(
                    message = when (serviceState) {
                        is ServiceState.Starting -> "Iniciando servicio..."
                        is ServiceState.Capturing -> "Capturando pantalla..."
                        is ServiceState.Stopping -> "Deteniendo servicio..."
                        else -> "Cargando..."
                    }
                )
            }
            is ServiceState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: ${(serviceState as ServiceState.Error).message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Button(
                        onClick = { viewModel.requestPermission() }
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Error dialog
    uiState.errorMessage?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = { viewModel.clearError() }
        )
    }
}

@Composable
fun SettingsCard(
    captureMode: Int,
    captureOnOpen: Boolean,
    onModeChanged: (Int) -> Unit,
    onToggleOnOpen: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Configuración", style = MaterialTheme.typography.titleMedium)
            }
            
            Divider()

            Text("Modo de Captura", style = MaterialTheme.typography.bodyMedium)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = captureMode == ScreenshotService.MODE_ACTIVE_SCREEN,
                    onClick = { if (enabled) onModeChanged(ScreenshotService.MODE_ACTIVE_SCREEN) },
                    enabled = enabled
                )
                Text("Pantalla Activa", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = captureMode == ScreenshotService.MODE_NOTIFICATIONS,
                    onClick = { if (enabled) onModeChanged(ScreenshotService.MODE_NOTIFICATIONS) },
                    enabled = enabled
                )
                Text("Solo Notificaciones (Delay 3s)", modifier = Modifier.padding(start = 8.dp))
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Capturar al abrir App", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = captureOnOpen,
                    onCheckedChange = onToggleOnOpen
                )
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(serviceState: ServiceState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Estado del Servicio",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = when (serviceState) {
                        is ServiceState.Idle -> "Inactivo"
                        is ServiceState.Starting -> "Iniciando..."
                        is ServiceState.Active -> "Activo"
                        is ServiceState.Capturing -> "Capturando..."
                        is ServiceState.Stopping -> "Deteniendo..."
                        is ServiceState.Error -> "Error"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (serviceState) {
                            is ServiceState.Active -> Color.Green
                            is ServiceState.Error -> MaterialTheme.colorScheme.error
                            is ServiceState.Idle -> Color.Gray
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
        }
    }
}

@Composable
private fun StartServiceButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.8f),
        enabled = !isLoading,
        contentPadding = PaddingValues(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Iniciar Servicio")
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Tomar Captura",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
