package com.team.notify.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = SpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("spaceId"),
        Index("updatedAt"),
        Index("isSynced"),
    ],
)
data class PageEntity(
    @PrimaryKey
    val id: String,
    val spaceId: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val isSynced: Boolean,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)
