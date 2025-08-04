package com.example.shotly.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class para representar un screenshot
data class Screenshot(
    val id: Int,
    val timestamp: String,
    val imageRes: Int? = null // En una app real, sería una URL o bitmap
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsScreen() {
    val screenshots = remember {
        listOf(
            Screenshot(1, "9:41"),
            Screenshot(2, "8:41"),
            Screenshot(3, "8:41"),
            Screenshot(4, "8:40"),
            Screenshot(5, "8:39"),
            Screenshot(6, "8:38")
        )
    }

    var selectedScreenshots by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedScreenshots.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6B46C1)) // Purple background
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "Screenshots",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = { /* Handle back */ }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Handle menu */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6B46C1)
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.White,
                    RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .padding(16.dp)
        ) {
            // Take Screenshot Button
            Button(
                onClick = { /* Handle take screenshot */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "Take Screenshot",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Section
            Text(
                "Recent",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Screenshots Grid
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(screenshots.chunked(3)) { rowScreenshots ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowScreenshots.forEach { screenshot ->
                            ScreenshotCard(
                                screenshot = screenshot,
                                isSelected = selectedScreenshots.contains(screenshot.id),
                                onSelectionChanged = { selected ->
                                    selectedScreenshots = if (selected) {
                                        selectedScreenshots + screenshot.id
                                    } else {
                                        selectedScreenshots - screenshot.id
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number of items
                        if (rowScreenshots.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Bottom Actions
            if (isSelectionMode) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    TextButton(
                        onClick = {
                            selectedScreenshots = screenshots.map { it.id }.toSet()
                        }
                    ) {
                        Text("Select All", color = Color(0xFF6B46C1))
                    }

                    TextButton(
                        onClick = {
                            // Handle delete
                            selectedScreenshots = emptySet()
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenshotCard(
    screenshot: Screenshot,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        onClick = {
            onSelectionChanged(!isSelected)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Placeholder para imagen del screenshot
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFFE5E7EB),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Screenshot",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        screenshot.timestamp,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Blue.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                )

                // Checkmark in corner
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            Color.Blue,
                            RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenshotsScreenPreview() {
    MaterialTheme {
        ScreenshotsScreen()
    }
}