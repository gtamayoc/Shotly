package com.example.shotly.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shotly.capture.Capture

@Composable
fun ImageGallery(
    captures: List<Capture>,
    onImageClick: (Capture) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2
) {
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = captures,
            key = { it.id }
        ) { capture ->
            ImageCard(
                capture = capture,
                onClick = { onImageClick(capture) }
            )
        }
    }
}

@Composable
fun ImageCard(
    capture: Capture,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // 3:4 aspect ratio
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(capture.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = capture.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlay con información
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.7f)
                    )
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = capture.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = capture.date,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    capture: Capture,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    capture.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )

        // Imagen en pantalla completa
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(capture.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = capture.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Información de la imagen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = capture.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = capture.date,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${capture.id}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Ejemplo de uso completo con navegación
@Composable
fun ImageGalleryScreen() {
    // Estado para manejar la navegación
    var selectedCapture by remember { mutableStateOf<Capture?>(null) }

    // Datos de ejemplo
    val sampleCaptures = remember {
        listOf(
            Capture(
                id = "1",
                uri = Uri.parse("https://picsum.photos/400/600?random=1"),
                title = "Captura 1",
                date = "18 Jul 2025 - 9:41"
            ),
            Capture(
                id = "2",
                uri = Uri.parse("https://picsum.photos/400/600?random=2"),
                title = "Screenshot Dashboard",
                date = "18 Jul 2025 - 9:35"
            ),
            Capture(
                id = "3",
                uri = Uri.parse("https://picsum.photos/400/600?random=3"),
                title = "App Settings",
                date = "18 Jul 2025 - 9:20"
            ),
            Capture(
                id = "4",
                uri = Uri.parse("https://picsum.photos/400/600?random=4"),
                title = "Profile Screen",
                date = "18 Jul 2025 - 9:15"
            ),
            Capture(
                id = "5",
                uri = Uri.parse("https://picsum.photos/400/600?random=5"),
                title = "Login Form",
                date = "18 Jul 2025 - 9:10"
            ),
            Capture(
                id = "6",
                uri = Uri.parse("https://picsum.photos/400/600?random=6"),
                title = "Home Screen",
                date = "18 Jul 2025 - 9:05"
            )
        )
    }

    // Navegación condicional
    if (selectedCapture != null) {
        ImageDetailScreen(
            capture = selectedCapture!!,
            onBackClick = { selectedCapture = null }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = "Galería de Capturas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Gallery
            ImageGallery(
                captures = sampleCaptures,
                onImageClick = { capture ->
                    selectedCapture = capture
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageGalleryPreview() {
    MaterialTheme {
        ImageGalleryScreen()
    }
}