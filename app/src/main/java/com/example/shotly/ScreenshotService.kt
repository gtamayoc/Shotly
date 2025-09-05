package com.example.shotly

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ScreenshotService : Service() {

    companion object {
        const val ACTION_START = "com.example.screenshotter.action.START"
        const val ACTION_CAPTURE = "com.example.screenshotter.action.CAPTURE"
        const val ACTION_STOP = "com.example.screenshotter.action.STOP"

        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_DATA_INTENT = "dataIntent"

        const val CHANNEL_ID = "screenshot_channel"
        const val NOTIF_ID = 1337
    }

    private lateinit var mpm: MediaProjectionManager
    private var projection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?) = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>(EXTRA_DATA_INTENT)
                if (resultCode == Activity.RESULT_OK && data != null) {
                    startInForeground("Inicializando…")
                    projection = mpm.getMediaProjection(resultCode, data)
                    updateNotification("Listo para capturar")
                } else stopSelf()
            }

            ACTION_CAPTURE -> takeScreenshotOnce()

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                projection?.stop()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startInForeground(content: String) {
        val notification = buildNotification(content)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIF_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else startForeground(NOTIF_ID, notification)
    }

    private fun buildNotification(content: String, imageUri: Uri? = null): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val captureIntent =
            Intent(this, ScreenshotService::class.java).apply { action = ACTION_CAPTURE }
        val capturePI = PendingIntent.getService(
            this, 1, captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenshotService::class.java).apply { action = ACTION_STOP }
        val stopPI = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Servicio de capturas")
            .setContentText(content)
            .setOngoing(true)
            .setContentIntent(contentPI)
            .addAction(android.R.drawable.ic_menu_camera, "Capturar", capturePI)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPI)

        imageUri?.let {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val sharePI = PendingIntent.getActivity(
                this, 3, Intent.createChooser(shareIntent, "Compartir captura"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_share, "Compartir", sharePI)
        }

        return builder.build()
    }

    private fun updateNotification(content: String, imageUri: Uri? = null) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(content, imageUri))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Capturas", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Notificaciones de capturas"
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
    }

    private fun takeScreenshotOnce() {
        val proj = projection ?: return
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        // 👉 REGISTRA EL CALLBACK ANTES
        proj.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                // liberar recursos si el sistema detiene la proyección
                try { imageReader.close() } catch (_: Exception) {}
                stopSelf()
            }
        }, Handler(Looper.getMainLooper()))

        val vDisplay = proj.createVirtualDisplay(
            "screencap", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
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

            val uri = saveBitmapToCache(bitmap)
            bitmap.recycle()
            updateNotification("Captura guardada", uri)

            image.close()
            reader.close()
            vDisplay.release()
        }, Handler(Looper.getMainLooper()))
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val cacheDir = File(cacheDir, "screenshots").apply { mkdirs() }
        val file = File(cacheDir, "screenshot_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }

    private fun saveBitmapToMediaStore(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            val name = "Screenshot_${System.currentTimeMillis()}.png"
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            resolver.openOutputStream(uri).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out!!)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
        return uri
    }
}