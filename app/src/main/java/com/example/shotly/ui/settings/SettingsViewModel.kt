package com.example.shotly.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shotly.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val captureMode: StateFlow<Int> = settingsRepository.captureMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val captureOnAppOpen: StateFlow<Boolean> = settingsRepository.captureOnAppOpen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val storageLocation: StateFlow<String> = settingsRepository.storageLocation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val privacyPolicyAccepted: StateFlow<Boolean> = settingsRepository.privacyPolicyAccepted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val analyticsEnabled: StateFlow<Boolean> = settingsRepository.analyticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateCaptureMode(mode: Int) {
        viewModelScope.launch {
            settingsRepository.setCaptureMode(mode)
        }
    }

    fun updateCaptureOnAppOpen(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCaptureOnAppOpen(enabled)
        }
    }

    fun updateStorageLocation(path: String) {
        viewModelScope.launch {
            settingsRepository.setStorageLocation(path)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun acceptPrivacyPolicy(accepted: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPrivacyPolicyAccepted(accepted)
        }
    }

    fun updateAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAnalyticsEnabled(enabled)
        }
    }
}
