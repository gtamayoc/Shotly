package com.example.shotly.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun CaptureThumbnail(
    imagePath: String,
    targetWidth: Int,
    targetHeight: Int,
    modifier: Modifier = Modifier
) {
    val bitmap by remember(imagePath, targetWidth, targetHeight) {
        // Importante: especificar el tipo <Bitmap?> para que no falle la inferencia
        mutableStateOf<Bitmap?>(
            decodeSampledBitmap(
                File(imagePath).absolutePath,
                targetWidth,
                targetHeight
            )
        )
    }

    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(targetWidth.dp)
        )
    }
}

fun decodeSampledBitmap(path: String, reqW: Int, reqH: Int): Bitmap? {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, opts)
    var inSampleSize = 1
    val (h, w) = opts.outHeight to opts.outWidth
    if (h > reqH || w > reqW) {
        val halfH = h / 2
        val halfW = w / 2
        while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
            inSampleSize *= 2
        }
    }
    val decodeOpts = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
    return BitmapFactory.decodeFile(path, decodeOpts)
}