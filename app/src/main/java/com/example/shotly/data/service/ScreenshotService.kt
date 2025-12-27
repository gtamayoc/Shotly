package com.example.shotly.data.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
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

import com.example.shotly.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Servicio optimizado para captura de pantalla en segundo plano
 * Soporta modos: Captura Activa (pantalla) y Captura Notificaciones.
 */
@AndroidEntryPoint
class ScreenshotService : Service() {
    
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var captureMode: Int = MODE_ACTIVE_SCREEN // Default

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
        const val ACTION_EXIT = "ACTION_EXIT"
        
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_DATA_INTENT = "data"
        const val EXTRA_CAPTURE_MODE = "captureMode"

        const val MODE_ACTIVE_SCREEN = 0
        const val MODE_NOTIFICATIONS = 1

        val serviceState = MutableStateFlow<ServiceState>(ServiceState.Idle)
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_capture"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                captureMode = intent.getIntExtra(EXTRA_CAPTURE_MODE, MODE_ACTIVE_SCREEN)
                serviceState.value = ServiceState.Starting
                startCaptureService(intent)
            }
            ACTION_CAPTURE -> {
                serviceState.value = ServiceState.Capturing
                handleCaptureRequest()
            }
            ACTION_EXIT -> {
                 stopForegroundService()
            }
        }

        return START_NOT_STICKY
    }


    private fun startCaptureService(intent: Intent) {
        try {
            // Obtener permisos y empezar proyección
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            val data = intent.getParcelableExtra<Intent>(EXTRA_DATA_INTENT)

            if (resultCode == Activity.RESULT_CANCELED || data == null) {
                serviceState.value = ServiceState.Error("Permisos de captura no concedidos", null)
                stopSelf()
                return
            }

            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification())

            val projectionManager = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            // Using WeakReference to callback to avoid strong reference cycle if native holds it?
            // Actually, just using Application Context for Manager often helps.
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
            Timber.d("Servicio de captura iniciado ($captureMode)")
            
        } catch (e: Exception) {
            Timber.e(e, "Error iniciando servicio de captura")
            serviceState.value = ServiceState.Error("Error al iniciar servicio: ${e.message}", e)
            stopSelf()
        }
    }

    private fun buildNotification(): android.app.Notification {
        val captureIntent = Intent(this, ScreenshotService::class.java).apply {
            action = ACTION_CAPTURE
        }
        val capturePendingIntent = PendingIntent.getService(
            this, 0, captureIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val exitIntent = Intent(this, ScreenshotService::class.java).apply {
            action = ACTION_EXIT
        }
        val exitPendingIntent = PendingIntent.getService(
            this, 1, exitIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (captureMode == MODE_NOTIFICATIONS) "Captura de Notificaciones" else "Captura de Pantalla"
        val text = if (captureMode == MODE_NOTIFICATIONS) "Toca para capturar en 3s" else "Toca para capturar pantalla activa"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(capturePendingIntent) // Click en notificacion captura
            .addAction(R.drawable.ic_launcher_foreground, "Apagar", exitPendingIntent) // Boton salir
            .setOngoing(true)
            .build()
    }

    private fun handleCaptureRequest() {
        if (captureMode == MODE_ACTIVE_SCREEN) {
            // Opción 1: Cerrar diálogos y capturar
             @Suppress("DEPRECATION")
             val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
             try {
                 sendBroadcast(closeIntent)
             } catch (e: Exception) {
                 Timber.w("Could not close system dialogs: ${e.message}")
             }
             
             // Esperar a que se cierre la barra - Usuario solicitó 3 segundos de espera
             Handler(Looper.getMainLooper()).postDelayed({
                 captureImage()
             }, 3000)
        } else {
            // Opción 2: Esperar 3s para notificaciones
            Handler(Looper.getMainLooper()).postDelayed({
                captureImage()
            }, 3000)
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
                // Intentar de nuevo en un momento breve si falló (buffer vacío)
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
        
        // Return cropped if needed, but for now full screen
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }

    private fun saveToGallery(bitmap: Bitmap) {
        // Obtener ruta personalizada síncronamente (en background thread del servicio)
        val storageUriString = runBlocking {
             try {
                 settingsRepository.storageLocation.first()
             } catch (e: Exception) {
                 ""
             }
        }
        
        // Interpretar carpeta
        val folderName = if (storageUriString.isNotEmpty()) {
             try {
                val uri = android.net.Uri.parse(storageUriString)
                val segment = uri.lastPathSegment?.split(":")?.lastOrNull()
                if (segment != null && segment.isNotBlank()) "Pictures/$segment" else "Pictures/Screenshots"
             } catch (e: Exception) {
                 "Pictures/Screenshots"
             }
        } else {
            "Pictures/Screenshots"
        }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "screenshot_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, folderName)
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
            serviceState.value = ServiceState.Stopping
            
            mediaProjection?.unregisterCallback(projectionCallback)
            mediaProjection?.stop()
            mediaProjection = null
            
            imageReader?.close()
            imageReader = null
            
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            
            serviceState.value = ServiceState.Idle
            Timber.d("Servicio detenido correctamente")
        } catch (e: Exception) {
            Timber.e(e, "Error deteniendo servicio")
            serviceState.value = ServiceState.Error("Error al detener: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
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
        try {
            if (mediaProjection != null) {
                mediaProjection?.unregisterCallback(projectionCallback)
                mediaProjection?.stop()
                mediaProjection = null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error destroying media projection")
        }
        
        try {
            imageReader?.close()
            imageReader = null
        } catch (e: Exception) {
            Timber.e(e, "Error closing image reader")
        }
        
        serviceState.value = ServiceState.Idle
        Timber.d("Servicio destruido y recursos liberados")
    }
}
