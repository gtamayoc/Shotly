package com.example.shotly.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val captureMode: Flow<Int>
    val captureOnAppOpen: Flow<Boolean>
    val storageLocation: Flow<String>
    val notificationsEnabled: Flow<Boolean>
    val privacyPolicyAccepted: Flow<Boolean>
    val analyticsEnabled: Flow<Boolean>

    suspend fun setCaptureMode(mode: Int)
    suspend fun setCaptureOnAppOpen(enabled: Boolean)
    suspend fun setStorageLocation(path: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setPrivacyPolicyAccepted(accepted: Boolean)
    suspend fun setAnalyticsEnabled(enabled: Boolean)
}
