package com.team.notify.taskflow.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS op_queue (
                    id TEXT NOT NULL PRIMARY KEY,
                    entityType TEXT NOT NULL,
                    entityId TEXT NOT NULL,
                    spaceId TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    operation TEXT NOT NULL,
                    payloadJson TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    retryCount INTEGER NOT NULL DEFAULT 0,
                    lastError TEXT
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_op_queue_timestamp ON op_queue(timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_op_queue_spaceId ON op_queue(spaceId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_op_queue_entityType_entityId ON op_queue(entityType, entityId)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS sync_state (
                    spaceId TEXT NOT NULL PRIMARY KEY,
                    lastPulledAt INTEGER NOT NULL DEFAULT 0
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
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pages_spaceId_updatedAt ON pages(spaceId, updatedAt)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS history (
                    id TEXT NOT NULL PRIMARY KEY,
                    spaceId TEXT NOT NULL,
                    entityType TEXT NOT NULL,
                    entityId TEXT NOT NULL,
                    field TEXT NOT NULL,
                    oldValue TEXT,
                    newValue TEXT,
                    userId TEXT,
                    timestamp INTEGER NOT NULL,
                    source TEXT NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_history_spaceId_timestamp ON history(spaceId, timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_history_entityType_entityId ON history(entityType, entityId)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS attachments (
                    id TEXT NOT NULL PRIMARY KEY,
                    spaceId TEXT NOT NULL,
                    entityType TEXT NOT NULL,
                    entityId TEXT NOT NULL,
                    localUri TEXT NOT NULL,
                    mimeType TEXT,
                    sizeBytes INTEGER,
                    remoteUrl TEXT,
                    status TEXT NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_spaceId_entityType_entityId ON attachments(spaceId, entityType, entityId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_status ON attachments(status)")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Users
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT NOT NULL PRIMARY KEY,
                    displayName TEXT,
                    email TEXT,
                    photoUrl TEXT,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())

            // Helper function to check if column exists
            fun columnExists(table: String, column: String): Boolean {
                val cursor = db.query("PRAGMA table_info($table)")
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(1) // column name is at index 1
                    if (columnName == column) {
                        cursor.close()
                        return true
                    }
                }
                cursor.close()
                return false
            }

            // Add columns only if they don't exist
            if (!columnExists("tasks", "titleUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN titleUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "descriptionUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN descriptionUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "statusUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN statusUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "deadlineUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN deadlineUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "completedUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN completedUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "assigneeId")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN assigneeId TEXT")
            }
            if (!columnExists("tasks", "assigneeUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN assigneeUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "labelsJson")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN labelsJson TEXT NOT NULL DEFAULT '[]'")
            }
            if (!columnExists("tasks", "labelsUpdatedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN labelsUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }

            db.execSQL("""
                UPDATE tasks
                SET titleUpdatedAt = CASE WHEN titleUpdatedAt = 0 THEN updatedAt ELSE titleUpdatedAt END,
                    descriptionUpdatedAt = CASE WHEN descriptionUpdatedAt = 0 THEN updatedAt ELSE descriptionUpdatedAt END,
                    statusUpdatedAt = CASE WHEN statusUpdatedAt = 0 THEN updatedAt ELSE statusUpdatedAt END,
                    deadlineUpdatedAt = CASE WHEN deadlineUpdatedAt = 0 THEN updatedAt ELSE deadlineUpdatedAt END,
                    completedUpdatedAt = CASE WHEN completedUpdatedAt = 0 THEN updatedAt ELSE completedUpdatedAt END,
                    assigneeUpdatedAt = CASE WHEN assigneeUpdatedAt = 0 THEN updatedAt ELSE assigneeUpdatedAt END,
                    labelsUpdatedAt = CASE WHEN labelsUpdatedAt = 0 THEN updatedAt ELSE labelsUpdatedAt END
            """.trimIndent())

            if (!columnExists("pages", "titleUpdatedAt")) {
                db.execSQL("ALTER TABLE pages ADD COLUMN titleUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("pages", "bodyUpdatedAt")) {
                db.execSQL("ALTER TABLE pages ADD COLUMN bodyUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
            db.execSQL("""
                UPDATE pages
                SET titleUpdatedAt = CASE WHEN titleUpdatedAt = 0 THEN updatedAt ELSE titleUpdatedAt END,
                    bodyUpdatedAt = CASE WHEN bodyUpdatedAt = 0 THEN updatedAt ELSE bodyUpdatedAt END
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_spaceId_updatedAt ON tasks(spaceId, updatedAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pages_spaceId_updatedAt ON pages(spaceId, updatedAt)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Helper function to check if column exists
            fun columnExists(table: String, column: String): Boolean {
                val cursor = db.query("PRAGMA table_info($table)")
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(1) // column name is at index 1
                    if (columnName == column) {
                        cursor.close()
                        return true
                    }
                }
                cursor.close()
                return false
            }

            // Add new columns to tasks table only if they don't exist
            if (!columnExists("tasks", "isSynced")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "isDeleted")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
            if (!columnExists("tasks", "deletedAt")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN deletedAt INTEGER")
            }
            if (!columnExists("tasks", "priority")) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'medium'")
            }
            
            // Create new indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_priority ON tasks(priority)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isSynced ON tasks(isSynced)")
            
            // Add space members table if not exists
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS space_members (
                    spaceId TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    role TEXT NOT NULL,
                    PRIMARY KEY(spaceId, userId)
                )
            """.trimIndent())
        }
    }

    fun all() = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
