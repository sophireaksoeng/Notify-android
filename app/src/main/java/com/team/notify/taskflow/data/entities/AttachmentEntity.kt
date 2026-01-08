package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    indices = [
        Index(value = ["spaceId", "entityType", "entityId"]),
        Index(value = ["status"])
    ]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val spaceId: String,
    val entityType: String,
    val entityId: String,
    val localUri: String,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val remoteUrl: String? = null,
    val status: String = "PENDING",
    val updatedAt: Long = System.currentTimeMillis()
)
