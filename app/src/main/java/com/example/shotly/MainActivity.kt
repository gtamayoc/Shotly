package com.example.shotly

import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.shotly.ui.theme.ShotlyTheme

class MainActivity : ComponentActivity() {
    private lateinit var mpm: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        setContent {
            ShotlyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(mpm = mpm)
                }
            }
        }
    }
}