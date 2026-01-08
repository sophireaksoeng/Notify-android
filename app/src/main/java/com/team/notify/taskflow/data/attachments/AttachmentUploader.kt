package com.team.notify.taskflow.data.attachments

import android.content.Context
import android.net.Uri

interface AttachmentUploader {
    suspend fun upload(
        context: Context,
        spaceId: String,
        attachmentId: String,
        localUri: Uri,
        mimeType: String?
    ): String
}
