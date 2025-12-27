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
import androidx.compose.material.icons.filled.Image
import com.example.shotly.ui.components.ErrorDialog
import com.example.shotly.ui.components.LoadingIndicator
import com.example.shotly.ui.components.ScreenshotItem
@Composable
fun ScreenshotsScreen(
    viewModel: ScreenshotsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedScreenshot by remember { mutableStateOf<com.example.shotly.domain.model.Screenshot?>(null) }

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
                    LoadingIndicator(androidx.compose.ui.res.stringResource(com.example.shotly.R.string.loading))
                }
            }
            uiState.screenshots.isEmpty() -> {
                EmptyState()
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.screenshots) { screenshot ->
                        ScreenshotItem(
                            screenshot = screenshot,
                            onClick = {
                                selectedScreenshot = it
                            },
                            onDelete = { viewModel.deleteScreenshot(it) }
                        )
                    }
                }
            }
        }
    }

    if (selectedScreenshot != null) {
        com.example.shotly.ui.components.ScreenshotPreview(
            screenshot = selectedScreenshot,
            onDismiss = { selectedScreenshot = null },
            onDelete = { 
                viewModel.deleteScreenshot(it.id)
                selectedScreenshot = null
            }
        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.shotly.R.string.no_screenshots),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.shotly.R.string.no_screenshots_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}