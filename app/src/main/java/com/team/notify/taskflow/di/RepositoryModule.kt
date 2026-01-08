package com.team.notify.taskflow.di

import com.team.notify.taskflow.data.repository.room.RoomPageRepository
import com.team.notify.taskflow.data.repository.room.RoomSpaceRepository
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.taskflow.data.repository.interfaces.SpaceRepository
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.team.notify.taskflow.data.repository.room.RoomTaskRepository
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
    abstract fun bindPageRepository(
        impl: RoomPageRepository
    ): PageRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: RoomTaskRepository
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSpaceRepository(
        impl: RoomSpaceRepository
    ): SpaceRepository
}
