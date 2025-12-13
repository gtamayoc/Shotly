package com.example.shotly.di

import android.content.Context
import androidx.room.Room
import com.example.shotly.data.local.database.ShotlyDatabase
import com.example.shotly.data.local.dao.ScreenshotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShotlyDatabase {
        return Room.databaseBuilder(
            context,
            ShotlyDatabase::class.java,
            "shotly_database"
        ).build()
    }

    @Provides
    fun provideScreenshotDao(database: ShotlyDatabase): ScreenshotDao {
        return database.screenshotDao()
    }
}