package com.team.notify.taskflow.data.sync

import com.team.notify.taskflow.data.dao.SpaceMemberDao
import javax.inject.Inject

class PermissionChecker @Inject constructor(
    private val memberDao: SpaceMemberDao
) {
    suspend fun canEdit(spaceId: String, userId: String): Boolean {
        val member = memberDao.getMember(spaceId, userId)
        return member?.role in listOf("OWNER", "EDITOR")
    }
}
