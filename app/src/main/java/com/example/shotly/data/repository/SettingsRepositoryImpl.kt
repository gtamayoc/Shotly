package com.example.shotly.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.shotly.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("shotly_settings", Context.MODE_PRIVATE)
    
    private val _captureMode = MutableStateFlow(prefs.getInt(KEY_CAPTURE_MODE, 0))
    override val captureMode: Flow<Int> = _captureMode.asStateFlow()

    private val _captureOnAppOpen = MutableStateFlow(prefs.getBoolean(KEY_CAPTURE_ON_OPEN, false))
    override val captureOnAppOpen: Flow<Boolean> = _captureOnAppOpen.asStateFlow()

    private val _storageLocation = MutableStateFlow(prefs.getString(KEY_STORAGE_LOCATION, "") ?: "")
    override val storageLocation: Flow<String> = _storageLocation.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    override val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _privacyPolicyAccepted = MutableStateFlow(prefs.getBoolean(KEY_PRIVACY_POLICY_ACCEPTED, false))
    override val privacyPolicyAccepted: Flow<Boolean> = _privacyPolicyAccepted.asStateFlow()

    private val _analyticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ANALYTICS_ENABLED, false))
    override val analyticsEnabled: Flow<Boolean> = _analyticsEnabled.asStateFlow()

    override suspend fun setCaptureMode(mode: Int) {
        prefs.edit().putInt(KEY_CAPTURE_MODE, mode).apply()
        _captureMode.value = mode
    }

    override suspend fun setCaptureOnAppOpen(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CAPTURE_ON_OPEN, enabled).apply()
        _captureOnAppOpen.value = enabled
    }

    override suspend fun setStorageLocation(path: String) {
        prefs.edit().putString(KEY_STORAGE_LOCATION, path).apply()
        _storageLocation.value = path
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    override suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_POLICY_ACCEPTED, accepted).apply()
        _privacyPolicyAccepted.value = accepted
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, enabled).apply()
        _analyticsEnabled.value = enabled
    }

    companion object {
        private const val KEY_CAPTURE_MODE = "capture_mode"
        private const val KEY_CAPTURE_ON_OPEN = "capture_on_open"
        private const val KEY_STORAGE_LOCATION = "storage_location"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_PRIVACY_POLICY_ACCEPTED = "privacy_policy_accepted"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
    }
}
