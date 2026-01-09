package com.team.notify.taskflow.di

import com.team.notify.taskflow.data.repository.UserProfileRepositoryImpl
import com.team.notify.taskflow.data.repository.interfaces.UserProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserProfileModule {

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository {
        return userProfileRepositoryImpl
    }
}
