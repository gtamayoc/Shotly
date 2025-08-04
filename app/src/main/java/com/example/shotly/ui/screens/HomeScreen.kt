package com.example.shotly.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.shotly.R
import com.example.shotly.ScreenshotService
import com.example.shotly.capture.Capture
import com.example.shotly.model.CapturesViewModel
import com.example.shotly.ui.theme.CapturasTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeScreen : ComponentActivity() {

    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
            1001
        )

        val projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        screenCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val intent = Intent(this, ScreenshotService::class.java).apply {
                        putExtra("resultCode", result.resultCode)
                        putExtra("data", data)
                    }
                    ContextCompat.startForegroundService(this, intent)
                }
            }

        // Uso en la Activity
        setContent {
            CapturasTheme {
                val viewModel = remember { CapturesViewModel() }
                val captures = viewModel.captures.value
                val isLoading = viewModel.isLoading.value

                LaunchedEffect(Unit) {
                    viewModel.loadInitialCaptures(contentResolver)
                }

                HomeScreen(
                    captures = captures,
                    isLoading = isLoading,
                    onLoadMore = {
                        viewModel.loadMoreCaptures(contentResolver)
                    },
                    onCaptureClick = {
                        val intent = projectionManager.createScreenCaptureIntent()
                        screenCaptureLauncher.launch(intent)
                    },
                    onItemClick = { capture ->
                        Log.d("HomeScreen", "Ítem seleccionado: ${capture.id}")
                    }
                )
            }
        }

//        // 3. Actualiza tu MainActivity
//        setContent {
//            CapturasTheme {
//                val viewModel = remember { CapturesViewModel() }
//                // Usar .value en lugar del operador by
//                val captures = viewModel.captures.value
//                val isLoading = viewModel.isLoading.value
//
//                // Cargar capturas iniciales
//                LaunchedEffect(Unit) {
//                    viewModel.loadInitialCaptures { limit, offset ->
//                        getCapturesFromGallery(limit, offset)
//                    }
//                }
//
//                if (captures != null) {
//                    if (isLoading != null) {
//                        HomeScreen(
//                            captures = captures,
//                            isLoading = isLoading,
//                            onLoadMore = {
//                                viewModel.loadMoreCaptures { limit, offset ->
//                                    getCapturesFromGallery(limit, offset)
//                                }
//                            },
//                            onCaptureClick = {
//                                val intent = projectionManager.createScreenCaptureIntent()
//                                screenCaptureLauncher.launch(intent)
//                            },
//                            onItemClick = { capture ->
//                                Log.d("HomeScreen", "Ítem seleccionado: ${capture.id}")
//                            }
//                        )
//                    }
//                }
//            }
//        }


//        setContent {
//            CapturasTheme {
//                val captures = remember { mutableStateListOf<Capture>() }
//
//                // Cargar capturas una vez al inicio
//                LaunchedEffect(Unit) {
//                    captures.clear()
//                    captures.addAll(getCapturesFromGallery())
//                }
//
//                HomeScreen(
//                    captures = captures,
//                    onCaptureClick = {
//                        val intent = projectionManager.createScreenCaptureIntent()
//                        screenCaptureLauncher.launch(intent)
//                    },
//                    onItemClick = { capture ->
//                        Log.d("HomeScreen", "Ítem seleccionado: ${capture.id}")
//                    }
//                )
//            }
//        }
    }

    private fun getCapturesFromGallery(
        limit: Int = 20,
        offset: Int = 0
    ): List<Capture> {
        val captures = mutableListOf<Capture>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("screenshot_%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"

        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val dateAddedSeconds = it.getLong(dateColumn)

                val contentUri = Uri.withAppendedPath(queryUri, id.toString())

                val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(dateAddedSeconds * 1000))

                captures.add(
                    Capture(
                        id = id.toString(),
                        uri = contentUri,
                        title = name,
                        date = formattedDate
                    )
                )
            }
        }

        return captures
    }

    private fun getCapturesFromGallery11(): List<Capture> {
        val captures = mutableListOf<Capture>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("screenshot_%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val dateAddedSeconds = it.getLong(dateColumn)

                val contentUri = Uri.withAppendedPath(queryUri, id.toString())

                val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(dateAddedSeconds * 1000))

                captures.add(
                    Capture(
                        id = id.toString(),
                        uri = contentUri,
                        title = name,
                        date = formattedDate
                    )
                )
            }
        }

        return captures
    }

    // 4. Actualiza tu HomeScreen para soportar lazy loading
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("NotConstructor")
    @Composable
    fun HomeScreen(
        captures: List<Capture>,
        isLoading: Boolean = false,
        onLoadMore: () -> Unit = {},
        onCaptureClick: () -> Unit,
        onItemClick: (Capture) -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Capturas de Pantalla") })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onCaptureClick) {
                    Icon(Icons.Default.Add, contentDescription = "Capturar")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyGalleryGrid(
                    captures = captures,
                    isLoading = isLoading,
                    onLoadMore = onLoadMore,
                    onItemClick = onItemClick
                )
            }
        }
    }

//    @OptIn(ExperimentalMaterial3Api::class)
//    @SuppressLint("NotConstructor")
//    @Composable
//    fun HomeScreen(
//        captures: List<Capture>,
//        onCaptureClick: () -> Unit,
//        onItemClick: (Capture) -> Unit
//    ) {
//        Scaffold(
//            topBar = {
//                TopAppBar(title = { Text("Capturas de Pantalla") })
//            },
//            floatingActionButton = {
//                FloatingActionButton(onClick = onCaptureClick) {
//                    Icon(Icons.Default.Add, contentDescription = "Capturar")
//                }
//            }
//        ) { innerPadding ->
//            // Contenido principal: galería en grilla
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//                GalleryGrid(
//                    captures = captures,
//                    onItemClick = onItemClick
//                )
//            }
//        }
//    }


    // Actualiza también tu LazyGalleryGrid con estas optimizaciones
    @Composable
    fun LazyGalleryGrid(
        captures: List<Capture>,
        isLoading: Boolean,
        onLoadMore: () -> Unit,
        onItemClick: (Capture) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val lazyGridState = rememberLazyGridState()

        // Configuración de fling behavior para scroll más suave
        val flingBehavior = ScrollableDefaults.flingBehavior()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 60.dp),
            state = lazyGridState,
            flingBehavior = flingBehavior,
            modifier = modifier,
            contentPadding = PaddingValues(3.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = true,
            // Optimizaciones adicionales para LazyVerticalGrid
        ) {
            itemsIndexed(
                items = captures,
                key = { _, capture -> capture.id },
                contentType = { _, _ -> "capture_item" } // Ayuda al reciclaje de views
            ) { index, capture ->
                CaptureGridItem(
                    capture = capture,
                    onClick = { onItemClick(capture) },
                    modifier = Modifier
                        .aspectRatio(0.7f)
                        .graphicsLayer {
                            // Esto puede ayudar con la performance de rendering
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                )

                // Trigger lazy loading cerca del final
                if (index >= captures.size - 9 && !isLoading) {
                    LaunchedEffect(Unit) {
                        onLoadMore()
                    }
                }
            }

            // Loading indicator al final
            if (isLoading) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    @Composable
    fun CaptureGridItem(
        capture: Capture,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(capture.uri)
                    .crossfade(200) // Transición suave entre imágenes
                    .memoryCachePolicy(CachePolicy.ENABLED) // Cache en memoria
                    .diskCachePolicy(CachePolicy.ENABLED) // Cache en disco
                    .build(),
                contentDescription = "Captura ${capture.id}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                // Placeholder mientras carga para evitar saltos
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
        }
    }




//    // 5. Crea un componente LazyGalleryGrid optimizado
//    @Composable
//    fun LazyGalleryGrid(
//        captures: List<Capture>,
//        isLoading: Boolean,
//        onLoadMore: () -> Unit,
//        onItemClick: (Capture) -> Unit
//    ) {
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(2),
//            contentPadding = PaddingValues(8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            itemsIndexed(captures) { index, capture ->
//                // Cargar más cuando llegue a los últimos 5 elementos
//                if (index >= captures.size - 5) {
//                    LaunchedEffect(captures.size) {
//                        onLoadMore()
//                    }
//                }
//
//                GalleryItem(
//                    capture = capture,
//                    onClick = { onItemClick(capture) }
//                )
//            }
//
//            // Mostrar indicador de carga al final
//            if (isLoading) {
//                item(span = { GridItemSpan(2) }) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }
//        }
//    }
//
//
//    // 6. Optimiza el componente individual de imagen
//    @Composable
//    fun GalleryItem(
//        capture: Capture,
//        onClick: () -> Unit
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .clickable { onClick() },
//            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//        ) {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(capture.uri)
//                    .crossfade(true)
//                    .size(300) // Limita el tamaño para optimizar memoria
//                    .build(),
//                contentDescription = capture.title,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop,
//                placeholder = painterResource(R.drawable.ic_launcher_background), // Agrega un placeholder
//                error = painterResource(R.drawable.ic_launcher_foreground) // Agrega imagen de error
//            )
//
//            // Overlay con información
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        Brush.verticalGradient(
//                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
//                            startY = 0.7f
//                        )
//                    )
//            ) {
//                Text(
//                    text = capture.date,
//                    color = Color.White,
//                    fontSize = 12.sp,
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(8.dp)
//                )
//            }
//        }
//    }


    @Composable
    fun CaptureCard1(capture: Capture, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = capture.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = capture.date, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    @Composable
    fun CaptureCard(capture: Capture, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model = capture.uri,
                    contentDescription = capture.title,
                    modifier = Modifier
                        .size(180.dp)
                        .padding(end = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = capture.title, style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                    Text(text = capture.date, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun GalleryGrid(
        captures: List<Capture>,
        onItemClick: (Capture) -> Unit
    ) {

        val imageLoader = ImageLoader.Builder(LocalContext.current)
            .crossfade(true)
            .respectCacheHeaders(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()

        LazyVerticalGrid(
            columns = GridCells.Fixed(6), // cambia a 100–180dp según tu gusto
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp),
            contentPadding = PaddingValues(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(captures) { capture ->
                AsyncImage(
                    model = capture.uri,
                    contentDescription = null,
                    imageLoader = imageLoader,
                    modifier = Modifier
                        .aspectRatio(1f) // cuadrada
                        .fillMaxWidth()
                        .clickable { onItemClick(capture) }
                )
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun PreviewHomeScreen() {
        val dummyList = listOf(
            Capture("1", Uri.EMPTY, "screenshot_1.png", "2025-01-01 10:00:00"),
            Capture("2", Uri.EMPTY, "screenshot_2.png", "2025-01-02 11:00:00")
        )
        CapturasTheme {
            HomeScreen(
                captures = dummyList,
                onCaptureClick = {},
                onItemClick = {}
            )
        }
    }

}
