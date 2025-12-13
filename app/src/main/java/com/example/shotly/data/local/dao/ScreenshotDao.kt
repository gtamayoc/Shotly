package com.example.shotly.data.local.dao

import androidx.room.*
import com.example.shotly.data.local.entity.ScreenshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {

    @Query("SELECT * FROM screenshots ORDER BY dateCreated DESC")
    fun getAllScreenshots(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE id = :id")
    suspend fun getScreenshotById(id: Long): ScreenshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshots(screenshots: List<ScreenshotEntity>)

    @Update
    suspend fun updateScreenshot(screenshot: ScreenshotEntity)

    @Delete
    suspend fun deleteScreenshot(screenshot: ScreenshotEntity)

    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteScreenshotById(id: Long)

    @Query("DELETE FROM screenshots WHERE id IN (:ids)")
    suspend fun deleteScreenshotsByIds(ids: List<Long>)

    @Query("DELETE FROM screenshots")
    suspend fun deleteAllScreenshots()
}
