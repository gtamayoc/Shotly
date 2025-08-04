package com.example.capturas.capture

import android.net.Uri

data class Capture(
    val id: String,
    val uri: Uri,
    val title: String,
    val date: String
)
