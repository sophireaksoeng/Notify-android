package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conflicts")
data class ConflictEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val localVersion: Int,
    val remoteVersion: Int,
    val resolved: Boolean = false,
    val timestamp: Long
)