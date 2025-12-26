package com.example.shotly.di

import android.content.ContentResolver
import android.content.Context
import android.media.projection.MediaProjectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemServiceModule {

    @Provides
    @Singleton
    fun provideMediaProjectionManager(
        @ApplicationContext context: Context
    ): MediaProjectionManager {
        return context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    
    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }
}