package com.example.shotly

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.shotly.ui.theme.CapturasTheme

class MainActivity : ComponentActivity() {

    lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

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

        setContent {
            BaseUi {
                val intent = projectionManager.createScreenCaptureIntent()
                screenCaptureLauncher.launch(intent)
            }
        }
    }

    @Composable
    fun BaseUi(onCaptura: () -> Unit = {}) {
        CapturasTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize()
                ) {
                    Text("Captura de pantallas", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onCaptura) {
                        Text("Iniciar captura")
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun PantallaCapturaPreview() {
        BaseUi()
    }
}