package com.example.shotly.domain.model

sealed class ServiceState {
    object Idle : ServiceState()
    object Starting : ServiceState()
    object Active : ServiceState()
    object Capturing : ServiceState()
    object Stopping : ServiceState()
    data class Error(val message: String, val throwable: Throwable? = null) : ServiceState()
}