package com.example.shotly.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.example.shotly.domain.model.Screenshot
import com.example.shotly.domain.repository.ScreenshotRepository
import com.example.shotly.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de screenshots
 * Lee capturas existentes desde MediaStore (no usa Room)
 * Basado en el CapturesRepository original con cache inteligente
 */
@Singleton
class ScreenshotRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : ScreenshotRepository {
    
    private var cache: List<Screenshot>? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityMs = 30000 // 30 segundos
    
    override suspend fun getAllScreenshots(): Flow<Result<List<Screenshot>>> = flow {
        emit(Result.Loading)
        try {
            val currentTime = System.currentTimeMillis()
            
            // Si no hay cache o está desactualizado, cargar todo
            if (cache == null || currentTime - lastFetchTime > cacheValidityMs) {
                cache = fetchScreenshotsFromMediaStore()
                lastFetchTime = currentTime
                Timber.d("Cache actualizado: ${cache?.size} capturas encontradas")
            } else {
                Timber.d("Usando cache: ${cache?.size} capturas")
            }
            
            emit(Result.Success(cache ?: emptyList()))
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo capturas")
            emit(Result.Error(e))
        }
    }
    
    private fun fetchScreenshotsFromMediaStore(): List<Screenshot> {
        val screenshots = mutableListOf<Screenshot>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE
        )
        
        // Buscar todas las imágenes que empiecen con "screenshot_"
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("screenshot_%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            
            while (it.moveToNext()) {
                try {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "unknown"
                    val path = it.getString(pathColumn) ?: ""
                    val dateAdded = Date(it.getLong(dateAddedColumn) * 1000)
                    val dateModified = Date(it.getLong(dateModifiedColumn) * 1000)
                    val size = it.getLong(sizeColumn)
                    val width = it.getInt(widthColumn)
                    val height = it.getInt(heightColumn)
                    val mimeType = it.getString(mimeTypeColumn) ?: "image/png"
                    
                    screenshots.add(
                        Screenshot(
                            id = id,
                            fileName = name,
                            filePath = path,
                            displayName = name,
                            dateCreated = dateAdded,
                            dateModified = dateModified,
                            size = size,
                            width = width,
                            height = height,
                            mimeType = mimeType
                        )
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Error procesando captura")
                }
            }
        }
        
        return screenshots
    }
    
    override suspend fun getScreenshotById(id: Long): Result<Screenshot?> {
        return try {
            // Buscar primero en cache
            val screenshot = cache?.find { it.id == id }
            if (screenshot != null) {
                Result.Success(screenshot)
            } else {
                // Si no está en cache, buscar en MediaStore
                val uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                
                val cursor = contentResolver.query(
                    uri,
                    arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.DATE_MODIFIED,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.WIDTH,
                        MediaStore.Images.Media.HEIGHT,
                        MediaStore.Images.Media.MIME_TYPE
                    ),
                    null,
                    null,
                    null
                )
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        val foundScreenshot = Screenshot(
                            id = it.getLong(0),
                            fileName = it.getString(1) ?: "unknown",
                            filePath = it.getString(2) ?: "",
                            displayName = it.getString(1) ?: "unknown",
                            dateCreated = Date(it.getLong(3) * 1000),
                            dateModified = Date(it.getLong(4) * 1000),
                            size = it.getLong(5),
                            width = it.getInt(6),
                            height = it.getInt(7),
                            mimeType = it.getString(8) ?: "image/png"
                        )
                        Result.Success(foundScreenshot)
                    } else {
                        Result.Success(null)
                    }
                } ?: Result.Success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo captura por ID: $id")
            Result.Error(e)
        }
    }
    
    override suspend fun saveScreenshot(screenshot: Screenshot): Result<Long> {
        // No necesario - las capturas se guardan directamente en MediaStore por el servicio
        return Result.Error(
            UnsupportedOperationException("Use ScreenshotService para capturar pantallas")
        )
    }
    
    override suspend fun deleteScreenshot(id: Long): Result<Unit> {
        return try {
            val uri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toString()
            )
            val deleted = contentResolver.delete(uri, null, null)
            
            if (deleted > 0) {
                cache = null // Invalidar cache
                Timber.d("Captura eliminada: $id")
                Result.Success(Unit)
            } else {
                Result.Error(Exception("No se pudo eliminar la captura"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error eliminando captura: $id")
            Result.Error(e)
        }
    }
    
    override suspend fun deleteScreenshots(ids: List<Long>): Result<Unit> {
        return try {
            var deletedCount = 0
            ids.forEach { id ->
                val uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                deletedCount += contentResolver.delete(uri, null, null)
            }
            
            cache = null // Invalidar cache
            Timber.d("Capturas eliminadas: $deletedCount de ${ids.size}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error eliminando capturas")
            Result.Error(e)
        }
    }
    
    override suspend fun updateScreenshot(screenshot: Screenshot): Result<Unit> {
        // No necesario - las capturas en MediaStore son read-only para esta app
        return Result.Error(
            UnsupportedOperationException("Las capturas de MediaStore son de solo lectura")
        )
    }
    
    /**
     * Invalida el cache manualmente
     * Útil después de capturar una nueva screenshot
     */
    fun invalidateCache() {
        cache = null
        lastFetchTime = 0
        Timber.d("Cache invalidado")
    }
}
