package com.example.shotly.ui.screenshots

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shotly.ui.components.ErrorDialog
import com.example.shotly.ui.components.LoadingIndicator
import com.example.shotly.ui.components.ScreenshotItem

@Composable
fun ScreenshotsScreen(
    viewModel: ScreenshotsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadScreenshots()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator("Cargando capturas...")
                }
            }
            uiState.screenshots.isEmpty() -> {
                EmptyState()
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.screenshots) { screenshot ->
                        ScreenshotItem(
                            screenshot = screenshot,
                            onClick = {
                                // Navigate to detail/editor
                            },
                            onDelete = { viewModel.deleteScreenshot(it) }
                        )
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = { viewModel.clearError() }
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No hay capturas",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Toma tu primera captura desde la pantalla principal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}