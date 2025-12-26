package com.example.shotly.data.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.example.shotly.R
import com.example.shotly.domain.model.ServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * Servicio optimizado para captura de pantalla en segundo plano
 * Restaurado del commit original y adaptado a la nueva arquitectura
 */
class ScreenshotService : Service() {
    
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    
    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            Timber.d("MediaProjection se detuvo")
            serviceState.value = ServiceState.Idle
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_CAPTURE = "ACTION_CAPTURE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_DATA_INTENT = "data"
        
        val serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                serviceState.value = ServiceState.Starting
                startCaptureService(intent)
            }
            ACTION_CAPTURE -> {
                serviceState.value = ServiceState.Capturing
                captureScreen()
            }
            ACTION_STOP -> {
                serviceState.value = ServiceState.Stopping
                stopForegroundService()
            }
        }

        return START_NOT_STICKY
    }

    private fun startCaptureService(intent: Intent) {
        try {
            // Crear notificación y poner servicio en foreground
            createNotificationChannel()
            startForeground(
                1, 
                NotificationCompat.Builder(this, "channel")
                    .setContentTitle("Servicio de captura activo")
                    .setContentText("Listo para tomar capturas de pantalla")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()
            )

            // Obtener permisos y empezar proyección
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            val data = intent.getParcelableExtra<Intent>(EXTRA_DATA_INTENT)

            if (resultCode == Activity.RESULT_CANCELED || data == null) {
                serviceState.value = ServiceState.Error("Permisos de captura no concedidos", null)
                stopSelf()
                return
            }

            val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            mediaProjection?.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))

            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            mediaProjection?.createVirtualDisplay(
                "ScreenCapture", 
                width, 
                height, 
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface, 
                null, 
                null
            )

            serviceState.value = ServiceState.Active
            Timber.d("Servicio de captura iniciado correctamente")
            
        } catch (e: Exception) {
            Timber.e(e, "Error iniciando servicio de captura")
            serviceState.value = ServiceState.Error("Error al iniciar servicio: ${e.message}", e)
            stopSelf()
        }
    }

    private fun captureScreen() {
        try {
            // Pequeño retraso para que la imagen esté lista
            Handler(Looper.getMainLooper()).postDelayed({
                captureImage()
            }, 500)
        } catch (e: Exception) {
            Timber.e(e, "Error capturando pantalla")
            serviceState.value = ServiceState.Error("Error al capturar: ${e.message}", e)
            serviceState.value = ServiceState.Active // Volver a activo
        }
    }

    private fun captureImage() {
        try {
            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                val bitmap = convertToBitmap(image)
                saveToGallery(bitmap)
                image.close()
                
                serviceState.value = ServiceState.Active
                Timber.d("Captura guardada exitosamente")
            } else {
                Timber.w("No se pudo obtener imagen")
                serviceState.value = ServiceState.Active
            }
        } catch (e: Exception) {
            Timber.e(e, "Error procesando imagen")
            serviceState.value = ServiceState.Error("Error procesando imagen: ${e.message}", e)
            serviceState.value = ServiceState.Active
        }
    }

    private fun convertToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }

    private fun saveToGallery(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "screenshot_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            uri?.let {
                contentResolver.openOutputStream(it).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out!!)
                }
                Timber.d("Imagen guardada en: $uri")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error guardando imagen")
            throw e
        }
    }

    private fun stopForegroundService() {
        try {
            mediaProjection?.unregisterCallback(projectionCallback)
            mediaProjection?.stop()
            mediaProjection = null
            
            imageReader?.close()
            imageReader = null
            
            serviceState.value = ServiceState.Idle
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            
            Timber.d("Servicio detenido correctamente")
        } catch (e: Exception) {
            Timber.e(e, "Error deteniendo servicio")
            serviceState.value = ServiceState.Error("Error al detener: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel",
                "Capturas de Pantalla",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para servicio de capturas"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.stop()
        imageReader?.close()
        serviceState.value = ServiceState.Idle
        Timber.d("Servicio destruido")
    }
}
