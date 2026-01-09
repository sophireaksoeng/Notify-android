package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val spaceId: String,
    val lastPulledAt: Long = 0L
)
