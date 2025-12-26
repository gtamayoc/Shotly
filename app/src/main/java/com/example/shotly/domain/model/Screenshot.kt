package com.example.shotly.domain.model

import java.util.Date

data class Screenshot(
    val id: Long = 0L,
    val fileName: String,
    val filePath: String,
    val displayName: String,
    val dateCreated: Date,
    val dateModified: Date,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String = "image/png"
) {
    val aspectRatio: Float get() = if (height != 0) width.toFloat() / height.toFloat() else 1f

    val sizeInMB: Float get() = size / (1024f * 1024f)

    fun isValidImage(): Boolean = width > 0 && height > 0 && filePath.isNotEmpty()
}