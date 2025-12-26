package com.example.shotly

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ShotlyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber para logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("Shotly Application iniciada")
        
        // Crear canal de notificaciones para el servicio
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel",
                "Capturas de Pantalla",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para servicio de capturas de pantalla"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            
            Timber.d("Canal de notificaciones creado")
        }
    }
}
