package com.example.shotly.data.repository

import android.content.Intent
import com.example.shotly.domain.model.ServiceState
import com.example.shotly.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MediaProjectionRepository {
    fun getServiceState(): Flow<ServiceState>
    suspend fun requestPermission(): Result<Intent>
    suspend fun startService(resultCode: Int, data: Intent): Result<Unit>
    suspend fun captureScreen(): Result<String>
    suspend fun stopService(): Result<Unit>
}