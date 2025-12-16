package com.team.notify.taskflow.data.sync

import android.util.Log
import com.team.notify.taskflow.data.entities.TaskEntity

object SyncConflictResolver {

    fun resolveTaskConflict(
        local: TaskEntity,
        remote: TaskEntity
    ): TaskEntity {
        if (local.updatedAt != remote.updatedAt) {
            Log.d(
                "SYNC_CONFLICT",
                "Local v${local.updatedAt} vs Remote v${remote.updatedAt}"
            )
        }

        return if (local.updatedAt >= remote.updatedAt) local else remote
    }
}