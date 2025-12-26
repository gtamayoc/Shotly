package com.example.shotly.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}
