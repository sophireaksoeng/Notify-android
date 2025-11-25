package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "op_queue")
data class OperationEntity(
    @PrimaryKey val id: String,
    val entityId: String,
    val entityType: String,
    val opType: String,
    val payloadJson: String,
    val timestamp: Long
)
