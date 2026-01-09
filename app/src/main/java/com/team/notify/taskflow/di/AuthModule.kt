package com.team.notify.taskflow.di

import com.google.firebase.auth.FirebaseAuth
import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.auth.FirebaseCurrentUserProvider
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
    abstract fun bindCurrentUserProvider(impl: FirebaseCurrentUserProvider): CurrentUserProvider
}
