package com.team.notify.taskflow.di

import android.content.Context
import androidx.room.Room
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.Migrations
import com.team.notify.taskflow.data.dao.*
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
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "notify.db")
            .addMigrations(*Migrations.all())
            .build()

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideSpaceDao(db: AppDatabase): SpaceDao = db.spaceDao()
    @Provides fun providePageDao(db: AppDatabase): PageDao = db.pageDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideOpQueueDao(db: AppDatabase): OpQueueDao = db.opQueueDao()
    @Provides fun provideSyncStateDao(db: AppDatabase): SyncStateDao = db.syncStateDao()
    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
    @Provides fun provideAttachmentDao(db: AppDatabase): AttachmentDao = db.attachmentDao()
    @Provides fun provideSpaceMemberDao(db: AppDatabase): SpaceMemberDao = db.spaceMemberDao()
}
