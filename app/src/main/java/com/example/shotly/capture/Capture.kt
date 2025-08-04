package com.example.shotly.capture

import android.net.Uri

data class Capture(
    val id: String,
    val uri: Uri,
    val title: String,
    val date: String
)
