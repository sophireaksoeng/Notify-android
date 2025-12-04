package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val entityId: String,
    val change: String,
    val timestamp: Long
)
