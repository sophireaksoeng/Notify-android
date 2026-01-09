package com.team.notify.taskflow.data.attachments

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.StorageReference
import com.team.notify.taskflow.data.sync.awaitCompat
import javax.inject.Inject

class FirebaseAttachmentUploader @Inject constructor(
    private val storageRoot: StorageReference
) : AttachmentUploader {

    override suspend fun upload(
        context: Context,
        spaceId: String,
        attachmentId: String,
        localUri: Uri,
        mimeType: String?
    ): String {
        val ref = storageRoot.child("spaces/$spaceId/attachments/$attachmentId")

        val meta = com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType(mimeType)
            .build()

        ref.putFile(localUri, meta).awaitCompat()
        val url = ref.downloadUrl.awaitCompat()
        return url.toString()
    }
}
