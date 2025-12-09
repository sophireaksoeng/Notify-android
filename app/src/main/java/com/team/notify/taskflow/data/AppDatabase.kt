package com.team.notify.taskflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.PageHistoryDao
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            "ALTER TABLE pages ADD COLUMN version INTEGER NOT NULL DEFAULT 1"
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS page_history (
                id TEXT PRIMARY KEY NOT NULL,
                pageId TEXT NOT NULL,
                version INTEGER NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [
        TaskEntity::class,
        SpaceEntity::class,
        OperationEntity::class,
        PageEntity::class,
        PageHistoryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun spaceDao(): SpaceDao
    abstract fun opQueueDao(): OpQueueDao
    abstract fun pageDao(): PageDao
    abstract fun pageHistoryDao(): PageHistoryDao

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
                    opType = "UPSERT",
                    payloadJson = "{}",
                    timestamp = now
                )
            }
            ops.forEach { opDao.insert(it) }
        }
    }
}