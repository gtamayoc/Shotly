package com.example.shotly

import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shotly.navigation.AppNavHost
import com.example.shotly.viewModel.SharedViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermission()
        setContent {
            val sharedVM: SharedViewModel = viewModel()
            sharedVM.mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            AppNavHost(sharedVM)
        }
    }

    private fun checkAndRequestPermission() {
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            saveToGallery()
        } else {
            requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                requestCode = 100
            )
        }
    }
}