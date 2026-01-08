package com.team.notify.taskflow.data.entities

import androidx.room.Entity

@Entity(
    tableName = "space_members",
    primaryKeys = ["spaceId", "userId"]
)
data class SpaceMemberEntity(
    val spaceId: String,
    val userId: String,
    val role: String // OWNER, EDITOR, VIEWER
)
