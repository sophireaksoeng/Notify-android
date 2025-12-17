package com.team.notify.taskflow.di

import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.auth.DefaultCurrentUserProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(
        impl: DefaultCurrentUserProvider
    ): CurrentUserProvider
}