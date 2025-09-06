package com.example.shotly


import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenshotService : Service() {

    companion object {
        const val ACTION_START = "com.example.screenshotter.action.START"
        const val ACTION_CAPTURE = "com.example.screenshotter.action.CAPTURE"
        const val ACTION_STOP = "com.example.screenshotter.action.STOP"

        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_DATA_INTENT = "dataIntent"

        const val CHANNEL_ID = "screenshot_channel"
        const val CHANNEL_NAME = "Capturas de pantalla"
        const val NOTIF_ID = 1337

        // ... otras constantes
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()
    }

    private lateinit var mpm: MediaProjectionManager
    private var projection: MediaProjection? = null

    private val imgThread = HandlerThread("screenshot-img").apply { start() }
    private val imgHandler = Handler(imgThread.looper)

    override fun onCreate() {
        super.onCreate()
        mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotificationChannel()
        _isServiceActive.value = false // Inicialmente inactivo
    }

    override fun onDestroy() {
        super.onDestroy()
        projection?.stop()
        imgThread.quitSafely()
        _isServiceActive.value = false // Marcar como inactivo
    }

    override fun onBind(intent: Intent?) = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>(EXTRA_DATA_INTENT)
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        startForeground(
                            NOTIF_ID,
                            buildNotification("Inicializando…"),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                        )
                        projection = mpm.getMediaProjection(resultCode, data).also {
                            // Registra el callback para saber cuándo se detiene la proyección
                            it.registerCallback(object : MediaProjection.Callback() {
                                override fun onStop() {
                                    stopSelf() // Detiene el servicio si el usuario revoca el permiso
                                }
                            }, imgHandler)
                        }
                        _isServiceActive.value = true
                        updateNotification("Listo para capturar")
                    } else {
                        startForeground(NOTIF_ID, buildNotification("Inicializando…"))
                    }

                    projection = mpm.getMediaProjection(resultCode, data)
                    updateNotification("Listo para capturar")
                } else {
                    stopSelf()
                }
            }

            ACTION_CAPTURE -> {
                if (projection == null) {
                    updateNotification("Permiso no disponible. Reinicia el servicio.")
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        takeScreenshotOnce()
                    }, 2500)
                }
            }

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                // Si se arranca sin acción, solo asegura notificación si ya había proyección
                if (projection != null) startInForeground()
            }
        }
        return START_STICKY
    }


    private fun startInForeground() {
        val notification = buildNotification("Inicializando…")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }


    private fun buildNotification(content: String): Notification {
        // PendingIntent para abrir la app
        val openAppIntent = Intent(this, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción: Capturar
        val captureIntent =
            Intent(this, ScreenshotService::class.java).apply { action = ACTION_CAPTURE }
        val capturePI = PendingIntent.getService(
            this, 1, captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción: Detener
        val stopIntent = Intent(this, ScreenshotService::class.java).apply { action = ACTION_STOP }
        val stopPI = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Servicio de capturas")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(contentPI)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_menu_camera,
                    "Tomar captura",
                    capturePI
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Detener",
                    stopPI
                )
            )
            .build()
    }

    private fun updateNotification(content: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Notificaciones del servicio de capturas"
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun takeScreenshotOnce() {
        val proj = projection ?: return

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val imageReader = ImageReader.newInstance(
            width,
            height,
            PixelFormat.RGBA_8888,
            2
        )

        var vDisplay: VirtualDisplay? = null

        try {
            // Antes de crear VirtualDisplay
            proj.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    // Aquí liberas recursos si el sistema detiene la captura
                    vDisplay?.release()
                    imageReader.close()
                    stopSelf() // Si quieres cerrar el servicio
                }
            }, imgHandler) // handler puede ser null si no necesitas uno específico


            vDisplay = proj.createVirtualDisplay(
                "screencap",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                imgHandler
            )

            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                try {
                    val plane = image.planes[0]
                    val buffer = plane.buffer
                    val pixelStride = plane.pixelStride
                    val rowStride = plane.rowStride
                    val rowPadding = rowStride - pixelStride * width

                    var bitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride,
                        height,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                    bitmap.recycle()
                    bitmap = cropped

                    val uri = saveBitmapToMediaStore(bitmap, width, height)
                    bitmap.recycle()

                    // ¡Aquí está la mejora!
                    if (uri != null) {
                        updateNotification("Captura guardada", uri)
                    } else {
                        updateNotification("Captura guardada (sin URI)")
                    }
                    // Opcional: podrías lanzar una notificación con la miniatura/acción compartir
                } catch (e: Exception) {
                    updateNotification("Error al capturar: ${e.message}")
                } finally {
                    image.close()
                    reader.setOnImageAvailableListener(null, null)
                    reader.close()
                    vDisplay?.release()
                }
            }, imgHandler)

            // Pequeño retraso opcional: el listener capturará cuando esté listo
        } catch (e: Exception) {
            updateNotification("Error creando VirtualDisplay: ${e.message}")
            try {
                imageReader.close()
            } catch (_: Exception) {
            }
            try {
                vDisplay?.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun saveBitmapToMediaStore(bitmap: Bitmap, width: Int, height: Int): Uri? {
        val resolver = contentResolver
        val time = System.currentTimeMillis()
        val name =
            "Screenshot_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(time))}.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.WIDTH, width)
            put(MediaStore.Images.Media.HEIGHT, height)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        var uri: Uri? = null
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        throw IOException("Fallo al comprimir PNG")
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            }
        } catch (e: Exception) {
            // Si falla, intenta limpiar el registro
            if (uri != null) {
                try {
                    resolver.delete(uri, null, null)
                } catch (_: Exception) {
                }
            }
            throw e
        }
        return uri
    }

    // 1. Modifica la firma de buildNotification para aceptar un URI opcional
    private fun buildNotification(content: String, imageUri: Uri? = null): Notification {
        // ... (código existente para contentPI, capturePI, stopPI)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            // ... (configuración existente)
            .setContentText(content)

        // Si se proporciona un URI de imagen, añade la acción "Ver"
        if (imageUri != null) {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                data = imageUri
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val viewPI = PendingIntent.getActivity(
                this, 3, viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(NotificationCompat.Action(
                android.R.drawable.ic_menu_view,
                "Ver",
                viewPI
            ))
        }

        return builder.build()
    }


    // 2. Modifica updateNotification para pasar el URI
    private fun updateNotification(content: String, imageUri: Uri? = null) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(content, imageUri))
    }


}