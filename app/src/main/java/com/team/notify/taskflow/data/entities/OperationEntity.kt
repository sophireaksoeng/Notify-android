package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "op_queue",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["spaceId"]),
        Index(value = ["entityType", "entityId"])
    ]
)
data class OperationEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val spaceId: String,
    val userId: String,
    val operation: String,
    val payloadJson: String,
    val timestamp: Long,
    val retryCount: Int = 0,
    val lastError: String? = null
)
