package com.example.shotly.data.mapper

import com.example.shotly.data.local.entity.ScreenshotEntity
import com.example.shotly.domain.model.Screenshot

fun ScreenshotEntity.toDomain(): Screenshot {
    return Screenshot(
        id = id,
        fileName = fileName,
        filePath = filePath,
        displayName = displayName,
        dateCreated = dateCreated,
        dateModified = dateModified,
        size = size,
        width = width,
        height = height,
        mimeType = mimeType
    )
}

fun Screenshot.toEntity(): ScreenshotEntity {
    return ScreenshotEntity(
        id = id,
        fileName = fileName,
        filePath = filePath,
        displayName = displayName,
        dateCreated = dateCreated,
        dateModified = dateModified,
        size = size,
        width = width,
        height = height,
        mimeType = mimeType
    )
}