package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [
        Index(value = ["spaceId", "timestamp"]),
        Index(value = ["entityType", "entityId"])
    ]
)
data class HistoryEntity(
    @PrimaryKey val id: String,
    val spaceId: String,
    val entityType: String,
    val entityId: String,
    val field: String,
    val oldValue: String?,
    val newValue: String?,
    val userId: String?,
    val timestamp: Long,
    val source: String
)
