package com.example.shotly.di

import com.example.shotly.data.repository.ScreenshotRepositoryImpl
import com.example.shotly.data.repository.MediaProjectionRepositoryImpl
import com.example.shotly.domain.repository.ScreenshotRepository
import com.example.shotly.domain.repository.MediaProjectionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideScreenshotRepository(
        screenshotRepositoryImpl: ScreenshotRepositoryImpl
    ): ScreenshotRepository {
        return screenshotRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideMediaProjectionRepository(
        mediaProjectionRepositoryImpl: MediaProjectionRepositoryImpl
    ): MediaProjectionRepository {
        return mediaProjectionRepositoryImpl
    }
}