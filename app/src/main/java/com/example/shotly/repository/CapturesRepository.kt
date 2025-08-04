package com.example.shotly.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.example.shotly.capture.Capture
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// SOLUCIÓN 3: Híbrida - Cache inteligente
class CapturesRepository {
    private var allCaptures: List<Capture>? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityMs = 30000 // 30 segundos

    fun getCapturesFromGallery(
        contentResolver: ContentResolver,
        limit: Int = 20,
        offset: Int = 0
    ): List<Capture> {
        val currentTime = System.currentTimeMillis()

        // Si no hay cache o está desactualizado, cargar todo
        if (allCaptures == null || currentTime - lastFetchTime > cacheValidityMs) {
            allCaptures = fetchAllCaptures(contentResolver)
            lastFetchTime = currentTime
        }

        // Paginar desde el cache
        return allCaptures?.drop(offset)?.take(limit) ?: emptyList()
    }

    private fun fetchAllCaptures(contentResolver: ContentResolver): List<Capture> {
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

    fun invalidateCache() {
        allCaptures = null
    }
}