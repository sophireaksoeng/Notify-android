package com.team.notify.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team.notify.data.local.NotifyDatabase
import com.team.notify.data.local.dao.PageDao
import com.team.notify.data.local.dao.SpaceDao
import com.team.notify.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE spaces ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE spaces ADD COLUMN deletedAt INTEGER")

            db.execSQL("ALTER TABLE pages ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE pages ADD COLUMN deletedAt INTEGER")

            db.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE tasks ADD COLUMN deletedAt INTEGER")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'medium'")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create new indexes with proper names to match the expected schema
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_priority` ON `tasks` (`priority`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_updatedAt` ON `tasks` (`updatedAt`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueDate` ON `tasks` (`dueDate`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_pageId` ON `tasks` (`pageId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_isSynced` ON `tasks` (`isSynced`)")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Ensure the priority index exists - this is the one that's missing
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_priority` ON `tasks` (`priority`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): NotifyDatabase {
        return try {
            Room.databaseBuilder(
                context,
                NotifyDatabase::class.java,
                "notify.db",
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
        } catch (e: Exception) {
            // If database creation fails, try with a fresh database
            context.deleteDatabase("notify.db")
            Room.databaseBuilder(
                context,
                NotifyDatabase::class.java,
                "notify.db",
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
        }
    }

    @Provides
    fun provideSpaceDao(db: NotifyDatabase): SpaceDao = db.spaceDao()

    @Provides
    fun providePageDao(db: NotifyDatabase): PageDao = db.pageDao()

    @Provides
    fun provideTaskDao(db: NotifyDatabase): TaskDao = db.taskDao()
}
