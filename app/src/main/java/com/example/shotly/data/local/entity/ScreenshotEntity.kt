package com.example.shotly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fileName: String,
    val filePath: String,
    val displayName: String,
    val dateCreated: Date,
    val dateModified: Date,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String
)