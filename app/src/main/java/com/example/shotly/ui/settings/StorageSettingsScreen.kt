package com.example.shotly.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StorageSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val storageLocation by viewModel.storageLocation.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Persist URI permissions (optional but good practice if we were using SAF fully)
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: Exception) {
                // Ignore if fails, we might just be using the name
            }

            // Extract a friendly name or just keep the URI string
            // For MediaStore integration, we prefer a folder name.
            // Simplified logic: Use the last segment or display name
            // For now, saving the full string. The Service will interpret it.
            // Or better: Let's attempt to get the display name here.
            
            // For this implementation step, we save the URI string.
            // We can parse it later or in ViewModel.
            // Let's just save the string.
            viewModel.updateStorageLocation(it.toString())
        }
    }
    
    // Helper to extract folder name for display
    val displayPath = remember(storageLocation) {
        if (storageLocation.isEmpty()) "Carpeta por defecto"
        else {
            try {
                val uri = android.net.Uri.parse(storageLocation)
                // Try to get easy to read end part
                 uri.lastPathSegment?.split(":")?.lastOrNull() ?: storageLocation
            } catch (e: Exception) {
                storageLocation
            }
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Almacenamiento") },
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
                .padding(16.dp)
        ) {
            Text(
                text = "Ubicación de Guardado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { launcher.launch(null) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = displayPath,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Toca para cambiar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Gestión de Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Aquí podrás gestionar (borrar, mover) tus capturas antiguas automáticamente en futuras actualizaciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
