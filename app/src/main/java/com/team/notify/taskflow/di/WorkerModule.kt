package com.team.notify.taskflow.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.dao.OpQueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    fun provideOpQueueDao(db: AppDatabase): OpQueueDao = db.opQueueDao()

    @Provides
    fun provideGson(): Gson = Gson()
}
