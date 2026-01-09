package com.team.notify.taskflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team.notify.taskflow.data.dao.*
import com.team.notify.taskflow.data.entities.*
import com.team.notify.data.local.entity.SpaceEntity as LocalSpaceEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        UserEntity::class,
        LocalSpaceEntity::class,
        PageEntity::class,
        TaskEntity::class,
        OperationEntity::class,
        SyncStateEntity::class,
        HistoryEntity::class,
        AttachmentEntity::class,
        SpaceMemberEntity::class,
        UserProfileEntity::class,
    ],
    version = 12, // Increment version for UserProfileEntity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun spaceDao(): SpaceDao
    abstract fun pageDao(): PageDao
    abstract fun taskDao(): TaskDao
    abstract fun opQueueDao(): OpQueueDao
    abstract fun syncStateDao(): SyncStateDao
    abstract fun historyDao(): HistoryDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun spaceMemberDao(): SpaceMemberDao
    abstract fun userProfileDao(): UserProfileDao


    companion object {
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "notify-db.db")
                .addCallback(AppDatabase.seedCallback())
                .fallbackToDestructiveMigration()
                .build()
        }

        fun seedCallback(): Callback {
            return object : Callback() {

                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedIfEmpty(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    seedIfEmpty(db)
                }

                private fun seedIfEmpty(db: SupportSQLiteDatabase) {
                    val c = db.query("SELECT COUNT(*) FROM spaces")
                    c.moveToFirst()
                    val count = c.getInt(0)
                    c.close()

                    if (count > 0) return

                    db.execSQL("""
                INSERT INTO spaces (id, name, ownerId, updatedAt, isSynced)
                VALUES ('notify-db', 'Notify Workspace', '', 0, true)
            """)

                    db.execSQL("""
                INSERT INTO pages (id, spaceId, title, content, version, createdAt, updatedAt)
                VALUES
                ('page-1', 'notify-db', 'Welcome', 'This is a demo page.\nExplore the UI like Notion.', 1, 0, 0),
                ('page-2', 'notify-db', 'Meeting Notes', '• Agenda\n• Decisions\n• Action items', 1, 0, 0),
                ('page-3', 'notify-db', 'Project Plan', 'Week 1–12 roadmap…', 1, 0, 0)
            """)

                    db.execSQL("""
                INSERT INTO tasks (
                    id, spaceId, title, description, status,
                    deadline, completed, assigneeUserId, labels,
                    titleUpdatedAt, descriptionUpdatedAt, statusUpdatedAt,
                    deadlineUpdatedAt, completedUpdatedAt, assigneeUpdatedAt, labelsUpdatedAt,
                    updatedAt
                )
                VALUES
                ('task-1', 'notify-db', 'Explore the UI', 'Click around like Notion', 'TODO',
                 NULL, 0, NULL, '[]',
                 0, 0, 0, 0, 0, 0, 0,
                 0),

                ('task-2', 'notify-db', 'Test dark mode', 'Switch system theme to dark', 'DOING',
                 NULL, 0, NULL, '[]',
                 0, 0, 0, 0, 0, 0, 0,
                 0),

                ('task-3', 'notify-db', 'Finish Role B/C UI', 'Drawer + tasks list + page editor', 'DONE',
                 NULL, 1, NULL, '[]',
                 0, 0, 0, 0, 0, 0, 0,
                 0)
            """)
                }
            }
        }

    }
}
