package com.team.notify.taskflow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team.notify.taskflow.data.dao.ConflictDao
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.PageHistoryDao
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.SpaceMemberDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.ConflictEntity
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        var hasVersion = false
        database.query("PRAGMA table_info('pages')").use { c ->
            val nameIndex = c.getColumnIndex("name")
            while (c.moveToNext()) {
                val colName = if (nameIndex != -1) c.getString(nameIndex) else null
                if (colName == "version") {
                    hasVersion = true
                    break
                }
            }
        }
        if (!hasVersion) {
            database.execSQL("ALTER TABLE pages ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
        }

        var hasIsLoading = false
        database.query("PRAGMA table_info('pages')").use { c ->
            val nameIndex = c.getColumnIndex("name")
            while (c.moveToNext()) {
                val colName = if (nameIndex != -1) c.getString(nameIndex) else null
                if (colName == "isLoading") {
                    hasIsLoading = true
                    break
                }
            }
        }
        if (!hasIsLoading) {
            database.execSQL("ALTER TABLE pages ADD COLUMN isLoading INTEGER NOT NULL DEFAULT 0")
        }

        var hasHasConflict = false
        database.query("PRAGMA table_info('pages')").use { c ->
            val nameIndex = c.getColumnIndex("name")
            while (c.moveToNext()) {
                val colName = if (nameIndex != -1) c.getString(nameIndex) else null
                if (colName == "hasConflict") {
                    hasHasConflict = true
                    break
                }
            }
        }
        if (!hasHasConflict) {
            database.execSQL("ALTER TABLE pages ADD COLUMN hasConflict INTEGER NOT NULL DEFAULT 0")
        }

        val existingOpQueueCols = mutableSetOf<String>()
        database.query("PRAGMA table_info('op_queue')").use { c ->
            val nameIndex = c.getColumnIndex("name")
            while (c.moveToNext()) {
                val colName = if (nameIndex != -1) c.getString(nameIndex) else null
                if (colName != null) existingOpQueueCols.add(colName)
            }
        }

        if (!existingOpQueueCols.contains("spaceId")) {
            database.execSQL("ALTER TABLE op_queue ADD COLUMN spaceId TEXT NOT NULL DEFAULT 'undefined'")
        }
        if (!existingOpQueueCols.contains("userId")) {
            database.execSQL("ALTER TABLE op_queue ADD COLUMN userId TEXT NOT NULL DEFAULT 'undefined'")
        }

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS space_members (
                spaceId TEXT NOT NULL,
                userId TEXT NOT NULL,
                role TEXT NOT NULL,
                PRIMARY KEY(spaceId, userId)
            )
            """.trimIndent()
        )

        val existingConflictsCols = mutableSetOf<String>()
        database.query("PRAGMA table_info('conflicts')").use { c ->
            val nameIndex = c.getColumnIndex("name")
            while (c.moveToNext()) {
                val colName = if (nameIndex != -1) c.getString(nameIndex) else null
                if (colName != null) existingConflictsCols.add(colName)
            }
        }

        if (existingConflictsCols.isEmpty()) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS conflicts (
                    id TEXT PRIMARY KEY NOT NULL,
                    entityType TEXT NOT NULL,
                    entityId TEXT NOT NULL,
                    localVersion INTEGER NOT NULL,
                    remoteVersion INTEGER NOT NULL,
                    resolved INTEGER NOT NULL DEFAULT 0,
                    timestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )
        } else if (!existingConflictsCols.contains("resolved")) {
            database.execSQL("ALTER TABLE conflicts ADD COLUMN resolved INTEGER NOT NULL DEFAULT 0")
        }
    }
}

@Database(
    entities = [
        TaskEntity::class,
        SpaceEntity::class,
        OperationEntity::class,
        PageEntity::class,
        PageHistoryEntity::class,
        SpaceMemberEntity::class,
        ConflictEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun spaceDao(): SpaceDao
    abstract fun opQueueDao(): OpQueueDao
    abstract fun pageDao(): PageDao
    abstract fun pageHistoryDao(): PageHistoryDao
    abstract fun spaceMemberDao(): SpaceMemberDao
    abstract fun conflictDao(): ConflictDao

    companion object {

        fun seedCallback(): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    val roomDb = AppDatabaseHolder.database
                        ?: return

                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(roomDb)

                        val tasks = roomDb.taskDao().getAllTasksDebug()
                        android.util.Log.d(
                            "AppDatabase",
                            "Debug tasks after seeding count=${tasks.size}, tasks=$tasks"
                        )
                    }
                }
            }
        }

        suspend fun seedDatabase(db: AppDatabase) {
            android.util.Log.d("AppDatabase", "Seeding test data...")
            val spaceDao = db.spaceDao()
            val taskDao = db.taskDao()
            val opDao = db.opQueueDao()

            val now = System.currentTimeMillis()

            val defaultSpace = SpaceEntity(
                id = "space-1",
                name = "Demo Space",
                description = "Space for seed test data",
                createdAt = now,
                updatedAt = now
            )
            spaceDao.insert(defaultSpace)

            val tasks = listOf(
                TaskEntity(
                    id = "task-1",
                    spaceId = defaultSpace.id,
                    title = "Buy groceries",
                    description = "Milk, bread, eggs",
                    status = "TODO",
                    deadline = now + 24 * 60 * 60 * 1000L,
                    isCompleted = false,
                    updatedAt = now
                ),
                TaskEntity(
                    id = "task-2",
                    spaceId = defaultSpace.id,
                    title = "Finish report",
                    description = "Monthly financial report",
                    status = "IN_PROGRESS",
                    deadline = now + 2 * 24 * 60 * 60 * 1000L,
                    isCompleted = false,
                    updatedAt = now
                ),
                TaskEntity(
                    id = "task-3",
                    spaceId = defaultSpace.id,
                    title = "Call client",
                    description = "Follow up on integration",
                    status = "DONE",
                    deadline = now - 24 * 60 * 60 * 1000L,
                    isCompleted = true,
                    updatedAt = now
                )
            )

            tasks.forEach { taskDao.insert(it) }

            val ops = tasks.map { task ->
                OperationEntity(
                    id = "op-${task.id}",
                    entityId = task.id,
                    entityType = "TASK",
                    spaceId = task.spaceId,
                    userId = "",
                    operation = "UPSERT",
                    payloadJson = "{}",
                    timestamp = now
                )
            }
            ops.forEach { opDao.insert(it) }
        }
    }
}