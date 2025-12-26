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

    override suspend fun setCaptureMode(mode: Int) {
        prefs.edit().putInt(KEY_CAPTURE_MODE, mode).apply()
        _captureMode.value = mode
    }

    override suspend fun setCaptureOnAppOpen(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CAPTURE_ON_OPEN, enabled).apply()
        _captureOnAppOpen.value = enabled
    }

    companion object {
        private const val KEY_CAPTURE_MODE = "capture_mode"
        private const val KEY_CAPTURE_ON_OPEN = "capture_on_open"
    }
}
