package com.team.notify

import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.repository.NotifyRepositoryImpl
import com.team.notify.fake.FakePageDao
import com.team.notify.fake.FakeSpaceDao
import com.team.notify.fake.FakeTaskDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotifyRepositoryImplTest {

    @Test
    fun upsertSpace_marksUnsyncedAndUpdatesTimestamp() = runTest {
        val spaceDao = FakeSpaceDao()
        val repo = NotifyRepositoryImpl(
            spaceDao = spaceDao,
            pageDao = FakePageDao(),
            taskDao = FakeTaskDao(),
        )

        val original = SpaceEntity(
            id = "s1",
            name = "Space",
            ownerId = "u1",
            updatedAt = 123L,
            isSynced = true,
        )

        repo.upsertSpace(original)

        val saved = spaceDao.getById("s1")!!
        assertFalse(saved.isSynced)
        assertEquals("Space", saved.name)
        assertNotEquals(123L, saved.updatedAt)
    }

    @Test
    fun upsertSpaceFromRemote_marksSynced() = runTest {
        val spaceDao = FakeSpaceDao()
        val repo = NotifyRepositoryImpl(
            spaceDao = spaceDao,
            pageDao = FakePageDao(),
            taskDao = FakeTaskDao(),
        )

        repo.upsertSpaceFromRemote(
            SpaceEntity(
                id = "s2",
                name = "Remote",
                ownerId = "u1",
                updatedAt = 999L,
                isSynced = false,
            )
        )

        val saved = spaceDao.getById("s2")!!
        assertEquals(true, saved.isSynced)
    }
}
