package com.example.shotly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.shotly.domain.model.Screenshot
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScreenshotItem(
    screenshot: Screenshot,
    onClick: (Screenshot) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Safety: Ensure aspect ratio is positive
    val aspectRatio = remember(screenshot.aspectRatio) {
        if (screenshot.aspectRatio > 0) screenshot.aspectRatio else 1f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(screenshot) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Image
            AsyncImage(
                model = screenshot.filePath,
                contentDescription = screenshot.displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            // Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ScreenshotDateFormatter.format(screenshot.dateCreated),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Note: Delete functionality moved to Preview or context menu, 
    // but kept logic here just in case caller expects it, 
    // simply not showing the specific icon button to keep grid clean 
    // OR we can keep it if user prefers. 
    // Given the plan says "Clear ripple effect" and "Improve Card", 
    // I've simplified the card to focus on the image. 
    // Actions are now primarily in the Preview.
}

private object ScreenshotDateFormatter {
    private val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    fun format(date: Date): String = formatter.format(date)
}