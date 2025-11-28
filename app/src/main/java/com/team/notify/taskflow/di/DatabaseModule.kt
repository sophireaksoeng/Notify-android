package com.team.notify.taskflow.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.repository.interfaces.TaskRepository
import com.team.notify.taskflow.repository.room.RoomTaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

//    @Provides
//    @Singleton
//    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "notionlite.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(db: AppDatabase) = db.taskDao()

//    @Provides
//    fun provideOpQueueDao(db: AppDatabase) = db.opQueueDao()

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: com.team.notify.taskflow.data.dao.TaskDao,
                              opQueueDao: com.team.notify.taskflow.data.dao.OpQueueDao,
                              gson: Gson): TaskRepository {
        return RoomTaskRepository(taskDao, opQueueDao, gson)
    }
}
