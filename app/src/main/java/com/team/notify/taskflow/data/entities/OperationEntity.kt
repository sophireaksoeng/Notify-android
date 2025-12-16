package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "op_queue")
data class OperationEntity(
    @PrimaryKey val id: String,
    val entityId: String,
    val entityType: String,
    val operation: String,
    val payloadJson: String,
    val timestamp: Long,
    val retryCount: Int = 0
)
