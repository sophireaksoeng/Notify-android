package com.team.notify.taskflow.di

import com.team.notify.taskflow.data.repository.TaskRepositoryImpl
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.team.notify.taskflow.data.repository.OpQueueRepositoryImpl
import com.team.notify.taskflow.data.repository.interfaces.OpQueueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindOpQueueRepository(
        impl: OpQueueRepositoryImpl
    ): OpQueueRepository
}
