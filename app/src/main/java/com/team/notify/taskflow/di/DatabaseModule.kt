package com.team.notify.taskflow.di

import android.content.Context
import androidx.room.Room
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.AppDatabaseHolder
import com.team.notify.taskflow.data.MIGRATION_6_7
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.PageHistoryDao
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.SpaceMemberDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.repository.RoomPageRepository
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.data.dao.ConflictDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "notify-db"
        )
            .addMigrations(MIGRATION_6_7)
            .addCallback(AppDatabase.seedCallback())
            .build()

        AppDatabaseHolder.database = db
        return db
    }

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideSpaceDao(db: AppDatabase): SpaceDao = db.spaceDao()

    @Provides
    fun provideSpaceMemberDao(db: AppDatabase): SpaceMemberDao = db.spaceMemberDao()

    @Provides
    fun provideConflictDao(db: AppDatabase): ConflictDao = db.conflictDao()

    @Provides
    fun provideOpQueueDao(db: AppDatabase): OpQueueDao = db.opQueueDao()

    @Provides
    fun providePageDao(db: AppDatabase): PageDao = db.pageDao()

    @Provides
    fun providePageHistoryDao(db: AppDatabase): PageHistoryDao = db.pageHistoryDao()

    @Provides
    @Singleton
    fun providePageRepository(
        pageDao: PageDao,
        pageHistoryDao: PageHistoryDao,
        opQueueDao: OpQueueDao,
        currentUserProvider: CurrentUserProvider
    ): PageRepository {
        return RoomPageRepository(pageDao, pageHistoryDao, opQueueDao, currentUserProvider)
    }
}
