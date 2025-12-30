package com.team.notify.taskflow.data.attachments

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.data.dao.AttachmentDao
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.entities.AttachmentEntity
import com.team.notify.taskflow.data.entities.OperationEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject
import java.util.UUID

@HiltWorker
class AttachmentUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val attachmentDao: AttachmentDao,
    private val opQueueDao: OpQueueDao,
    private val uploader: AttachmentUploader,
    private val currentUserProvider: CurrentUserProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val batch = attachmentDao.loadUploadQueue(limit = 5)
        if (batch.isEmpty()) return Result.success()

        for (a in batch) {
            try {
                attachmentDao.upsert(a.copy(status = "UPLOADING", updatedAt = System.currentTimeMillis()))

                val url = uploader.upload(
                    context = applicationContext,
                    spaceId = a.spaceId,
                    attachmentId = a.id,
                    localUri = Uri.parse(a.localUri),
                    mimeType = a.mimeType
                )

                val updated = a.copy(
                    remoteUrl = url,
                    status = "UPLOADED",
                    updatedAt = System.currentTimeMillis()
                )
                attachmentDao.upsert(updated)

                val payload = JSONObject().apply {
                    put("id", updated.id)
                    put("spaceId", updated.spaceId)
                    put("entityType", updated.entityType)
                    put("entityId", updated.entityId)
                    put("localUri", updated.localUri)
                    put("mimeType", updated.mimeType)
                    put("sizeBytes", updated.sizeBytes)
                    put("remoteUrl", updated.remoteUrl)
                    put("status", updated.status)
                    put("updatedAt", updated.updatedAt)
                }.toString()

                opQueueDao.insert(
                    OperationEntity(
                        id = UUID.randomUUID().toString(),
                        entityType = "ATTACHMENT",
                        entityId = updated.id,
                        spaceId = updated.spaceId,
                        userId = currentUserProvider.getCurrentUserId() ?: "unknown",
                        operation = "UPSERT",
                        payloadJson = payload,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                attachmentDao.upsert(a.copy(status = "FAILED", updatedAt = System.currentTimeMillis()))
                return Result.retry()
            }
        }

        return Result.success()
    }
}
