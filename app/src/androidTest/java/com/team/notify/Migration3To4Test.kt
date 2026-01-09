package com.team.notify

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.Migrations
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration3To4Test {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate3To4_keepsDataAndAddsColumns() {
        // Create DB with version 3 schema (minimal tasks/pages only)
        val db = helper.createDatabase(dbName, 3)

        // Create minimal tables matching v3 (enough for migration to run)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tasks (
                id TEXT NOT NULL PRIMARY KEY,
                spaceId TEXT NOT NULL,
                pageId TEXT,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT NOT NULL,
                deadline INTEGER,
                completed INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pages (
                id TEXT NOT NULL PRIMARY KEY,
                spaceId TEXT NOT NULL,
                title TEXT NOT NULL,
                body TEXT,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("INSERT INTO tasks (id, spaceId, title, status, completed, updatedAt) VALUES ('t1','s1','Hello','TODO',0,123)")
        db.execSQL("INSERT INTO pages (id, spaceId, title, updatedAt) VALUES ('p1','s1','Page',123)")

        db.close()

        // Run migration and validate
        helper.runMigrationsAndValidate(dbName, 4, true, Migrations.MIGRATION_3_4)
    }
}
