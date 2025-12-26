package com.example.shotly.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val captureMode: Flow<Int>
    val captureOnAppOpen: Flow<Boolean>

    suspend fun setCaptureMode(mode: Int)
    suspend fun setCaptureOnAppOpen(enabled: Boolean)
}
