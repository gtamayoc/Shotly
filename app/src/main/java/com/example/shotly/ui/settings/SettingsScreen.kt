package com.example.shotly.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SettingsItem(
            icon = Icons.Default.Storage,
            title = "Almacenamiento",
            subtitle = "Gestionar capturas guardadas",
            onClick = { /* TODO: Implementar */ }
        )

        HorizontalDivider()

        SettingsItem(
            icon = Icons.Default.CameraAlt,
            title = "Captura",
            subtitle = "Configurar opciones de captura",
            onClick = { /* TODO: Implementar */ }
        )

        HorizontalDivider()

        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Notificaciones",
            subtitle = "Configurar notificaciones",
            onClick = { /* TODO: Implementar */ }
        )

        HorizontalDivider()

        SettingsItem(
            icon = Icons.Default.Info,
            title = "Acerca de",
            subtitle = "Información de la aplicación",
            onClick = { /* TODO: Implementar */ }
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Versión 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}