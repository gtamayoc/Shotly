package com.example.shotly.data.repository

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.core.content.ContextCompat
import com.example.shotly.domain.repository.MediaProjectionRepository
import com.example.shotly.data.service.ScreenshotService
import com.example.shotly.domain.model.ServiceState
import com.example.shotly.domain.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaProjectionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaProjectionManager: MediaProjectionManager
) : MediaProjectionRepository {

    override fun getServiceState(): Flow<ServiceState> {
        return ScreenshotService.serviceState
    }

    override suspend fun requestPermission(): Result<Intent> {
        return try {
            val intent = mediaProjectionManager.createScreenCaptureIntent()
            Timber.d("Permission intent created")
            Result.Success(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error creating permission intent")
            Result.Error(e)
        }
    }

    override suspend fun startService(resultCode: Int, data: Intent, captureMode: Int): Result<Unit> {
        return try {
            val startIntent = Intent(context, ScreenshotService::class.java).apply {
                action = ScreenshotService.ACTION_START
                putExtra(ScreenshotService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScreenshotService.EXTRA_DATA_INTENT, data)
                putExtra(ScreenshotService.EXTRA_CAPTURE_MODE, captureMode)
            }
            ContextCompat.startForegroundService(context, startIntent)
            Timber.d("Service start command sent")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error starting service")
            Result.Error(e)
        }
    }

    override suspend fun captureScreen(): Result<String> {
        return try {
            val captureIntent = Intent(context, ScreenshotService::class.java).apply {
                action = ScreenshotService.ACTION_CAPTURE
            }
            ContextCompat.startForegroundService(context, captureIntent)
            Timber.d("Capture command sent")
            Result.Success("Capture initiated")
        } catch (e: Exception) {
            Timber.e(e, "Error capturing screen")
            Result.Error(e)
        }
    }

    override suspend fun stopService(): Result<Unit> {
        return try {
            val stopIntent = Intent(context, ScreenshotService::class.java).apply {
                action = ScreenshotService.ACTION_EXIT
            }
            context.startService(stopIntent)
            Timber.d("Service stop command sent")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error stopping service")
            Result.Error(e)
        }
    }
}