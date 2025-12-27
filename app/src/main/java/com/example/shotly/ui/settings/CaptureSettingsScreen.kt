package com.example.shotly.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CaptureSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val captureMode by viewModel.captureMode.collectAsState()
    val captureOnOpen by viewModel.captureOnAppOpen.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Configuración de Captura") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Modo de Captura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Radio buttons for capture mode (Example modes: 0 = Standard, 1 = High Quality)
            // Just an example implementation since mode is Int
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = captureMode == 0,
                    onClick = { viewModel.updateCaptureMode(0) }
                )
                Text("Estándar", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = captureMode == 1,
                    onClick = { viewModel.updateCaptureMode(1) }
                )
                Text("Alta Calidad (PNG)", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Comportamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Capturar al abrir",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Realizar captura automáticamente al abrir la app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = captureOnOpen,
                    onCheckedChange = { viewModel.updateCaptureOnAppOpen(it) }
                )
            }
        }
    }
}
