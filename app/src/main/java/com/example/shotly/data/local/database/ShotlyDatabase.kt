package com.example.shotly.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.shotly.data.local.dao.ScreenshotDao
import com.example.shotly.data.local.entity.ScreenshotEntity
import com.example.shotly.data.local.converter.DateConverter

@Database(
    entities = [ScreenshotEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class ShotlyDatabase : RoomDatabase() {
    abstract fun screenshotDao(): ScreenshotDao
}