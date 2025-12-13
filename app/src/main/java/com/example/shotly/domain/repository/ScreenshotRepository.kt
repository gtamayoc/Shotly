package com.example.shotly.domain.repository

import com.example.shotly.domain.model.Screenshot
import com.example.shotly.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ScreenshotRepository {
    suspend fun getAllScreenshots(): Flow<Result<List<Screenshot>>>
    suspend fun getScreenshotById(id: Long): Result<Screenshot?>
    suspend fun saveScreenshot(screenshot: Screenshot): Result<Long>
    suspend fun deleteScreenshot(id: Long): Result<Unit>
    suspend fun deleteScreenshots(ids: List<Long>): Result<Unit>
    suspend fun updateScreenshot(screenshot: Screenshot): Result<Unit>
}