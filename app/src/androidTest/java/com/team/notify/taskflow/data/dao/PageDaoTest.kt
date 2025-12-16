package com.team.notify.taskflow.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.entities.PageEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PageDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PageDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.pageDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertPage_thenRetrieve() = runTest {
        val page = PageEntity(
            id = "page-1",
            spaceId = "space-1",
            title = "Test page",
            content = "Test content",
            version = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        dao.upsert(page)
        val result = dao.getPageByIdOnce(page.id)

        assertEquals(page.title, result?.title)
    }
}
