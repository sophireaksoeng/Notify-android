package com.team.notify.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity

fun DocumentSnapshot.toSpaceEntity(): SpaceEntity? {
    val id = getString("id") ?: return null
    val name = getString("name") ?: ""
    val ownerId = getString("ownerId") ?: ""
    val updatedAt = getLong("updatedAt") ?: 0L
    val isDeleted = getBoolean("isDeleted") ?: false
    val deletedAt = getLong("deletedAt")

    return SpaceEntity(
        id = id,
        name = name,
        ownerId = ownerId,
        updatedAt = updatedAt,
        isSynced = true,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
    )
}

fun DocumentSnapshot.toPageEntity(): PageEntity? {
    val id = getString("id") ?: return null
    val spaceId = getString("spaceId") ?: return null
    val title = getString("title") ?: ""
    val content = getString("content") ?: ""
    val updatedAt = getLong("updatedAt") ?: 0L
    val isDeleted = getBoolean("isDeleted") ?: false
    val deletedAt = getLong("deletedAt")

    return PageEntity(
        id = id,
        spaceId = spaceId,
        title = title,
        content = content,
        updatedAt = updatedAt,
        isSynced = true,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
    )
}

fun DocumentSnapshot.toTaskEntity(): TaskEntity? {
    val id = getString("id") ?: return null
    val pageId = getString("pageId") ?: return null
    val title = getString("title") ?: ""
    val status = getString("status") ?: "TODO"
    val priority = getString("priority") ?: "medium"
    val updatedAt = getLong("updatedAt") ?: 0L
    val isDeleted = getBoolean("isDeleted") ?: false
    val deletedAt = getLong("deletedAt")

    return TaskEntity(
        id = id,
        pageId = pageId,
        title = title,
        status = status,
        dueDate = getLong("dueDate"),
        assigneeId = getString("assigneeId"),
        priority = priority,
        updatedAt = updatedAt,
        isSynced = true,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
    )
}
