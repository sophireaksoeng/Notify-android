package com.team.notify.taskflow.data.sync

import com.team.notify.taskflow.data.entities.TaskEntity

object LwwResolver {
    fun resolve(local: TaskEntity?, remote: TaskEntity): TaskEntity {
        if (local == null) return remote
        return if (remote.updatedAt > local.updatedAt) remote else local
    }
}
