package com.team.notify

import com.team.notify.taskflow.data.entities.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PerFieldConflictResolverTest {

    @Test
    fun mergesFieldByNewestTimestamp() {
        val base = TaskEntity.new(
            id = "t1",
            spaceId = "s1",
            title = "Old",
            description = "Desc",
            status = "TODO"
        )

        val local = base.copy(
            title = "LocalTitle",
            titleUpdatedAt = 200,
            updatedAt = 200
        )

        val remote = base.copy(
            title = "RemoteTitle",
            titleUpdatedAt = 150,
            status = "DONE",
            statusUpdatedAt = 300,
            updatedAt = 300
        )

        val merged = PerFieldConflictResolver.mergeTask(local, remote)

        // title: local wins (200 > 150)
        assertEquals("LocalTitle", merged.title)

        // status: remote wins (300 newer)
        assertEquals("DONE", merged.status)
    }
}
