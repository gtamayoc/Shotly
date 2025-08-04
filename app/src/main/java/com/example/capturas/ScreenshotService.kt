package com.example.capturas

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
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.capturas.capture.Capture

/**
 * Servicio optimizado para captura de pantalla en segundo plano
 */
class ScreenshotService : Service() {
    private lateinit var mediaProjection: MediaProjection
    private var imageReader: ImageReader? = null
    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            // Libera recursos o reinicia el servicio si se detuvo la proyección
            Log.d("ScreenshotService", "MediaProjection se detuvo.")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 🔐 Paso 1: Mostrar notificación inmediatamente
        createNotificationChannel()
        startForeground(
            1, NotificationCompat.Builder(this, "channel")
                .setContentTitle("Captura en curso")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )

        // ⚠️ Paso 2: Obtener permisos y empezar proyección después del foreground
        val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val data = intent.getParcelableExtra<Intent>("data")

        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data!!)

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        mediaProjection.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
        mediaProjection.createVirtualDisplay(
            "ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface, null, null
        )

        // Pequeño retraso para que la imagen esté lista
        Handler().postDelayed({ this.captureImage() }, 1000)

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel",
                "Capturas de Pantalla",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Canal para capturas"

            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }


    private fun captureImage() {
        val image = imageReader!!.acquireLatestImage()
        if (image != null) {
            val bitmap = convertToBitmap(image)
            saveToGallery(bitmap)
            image.close()
            Toast.makeText(this, "Captura guardada", Toast.LENGTH_SHORT).show()
        }
        stopSelf()
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
        val values = ContentValues()
        values.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "screenshot_" + System.currentTimeMillis() + ".png"
        )
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            contentResolver.openOutputStream(uri!!).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG, 100,
                    out!!
                )
            }
        } catch (e: Exception) {
            Log.e("ScreenshotService", "Error saving image", e)
        }
    }

    fun getCapturesFromGallery(): List<Capture> {
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

                val formattedDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(java.util.Date(dateAddedSeconds * 1000))

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


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}