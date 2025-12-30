package com.team.notify.taskflow.di

import com.team.notify.taskflow.data.attachments.AttachmentUploader
import com.team.notify.taskflow.data.attachments.FirebaseAttachmentUploader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AttachmentModule {
    @Binds
    @Singleton
    abstract fun bindUploader(impl: FirebaseAttachmentUploader): AttachmentUploader
}
