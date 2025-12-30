package com.team.notify.taskflow.data.backup

import android.content.Context
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {

    suspend fun exportJson(
        context: Context,
        db: AppDatabase,
        fileName: String = "notify-backup.json"
    ): String = withContext(Dispatchers.IO) {

        val spaces = db.spaceDao().getAllSpacesOnce()
        val pages = db.pageDao().getAllPagesDebug()
        val tasks = db.taskDao().getAllTasksDebug()

        val root = JSONObject()
            .put("version", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put("spaces", JSONArray(spaces.map { it.toJson() }))
            .put("pages", JSONArray(pages.map { it.toJson() }))
            .put("tasks", JSONArray(tasks.map { it.toJson() }))

        val file = File(context.filesDir, fileName)
        file.writeText(root.toString(2))
        file.absolutePath
    }

    suspend fun importJson(db: AppDatabase, path: String) =
        withContext(Dispatchers.IO) {

            val text = File(path).readText()
            val root = JSONObject(text)

            val spaces = root.getJSONArray("spaces").toSpaceList()
            val pages = root.getJSONArray("pages").toPageList()
            val tasks = root.getJSONArray("tasks").toTaskList()

            spaces.forEach { db.spaceDao().insert(it) }
            db.pageDao().upsertAll(pages)
            db.taskDao().upsertAll(tasks)
        }

    private fun SpaceEntity.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("name", name)
            .put("description", description)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)

    private fun PageEntity.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("spaceId", spaceId)
            .put("title", title)
            .put("content", content)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
            .put("hasConflict", hasConflict)

    private fun TaskEntity.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("spaceId", spaceId)
            .put("pageId", pageId)
            .put("title", title)
            .put("description", description)
            .put("status", status)
            .put("deadline", deadline)
            .put("isCompleted", isCompleted)
            .put("assigneeId", assigneeId)
            .put("labels", JSONArray(labels))
            .put("updatedAt", updatedAt)

    private fun JSONArray.toSpaceList(): List<SpaceEntity> =
        List(length()) { i ->
            val o = getJSONObject(i)
            SpaceEntity(
                id = o.getString("id"),
                name = o.getString("name"),
                description = o.optString("description").takeIf { it.isNotBlank() },
                createdAt = o.optLong("createdAt"),
                updatedAt = o.optLong("updatedAt")
            )
        }

    private fun JSONArray.toPageList(): List<PageEntity> =
        List(length()) { i ->
            val o = getJSONObject(i)
            PageEntity(
                id = o.getString("id"),
                spaceId = o.getString("spaceId"),
                title = o.getString("title"),
                content = o.optString("content"),
                createdAt = o.optLong("createdAt"),
                updatedAt = o.optLong("updatedAt"),
                hasConflict = o.optBoolean("hasConflict")
            )
        }

    private fun JSONArray.toTaskList(): List<TaskEntity> =
        List(length()) { i ->
            val o = getJSONObject(i)

            val labelsArr = o.optJSONArray("labels") ?: JSONArray()
            val labels = List(labelsArr.length()) { idx -> labelsArr.getString(idx) }

            TaskEntity(
                id = o.getString("id"),
                spaceId = o.getString("spaceId"),
                pageId = o.optString("pageId").takeIf { it.isNotBlank() },
                title = o.getString("title"),
                description = o.optString("description").takeIf { it.isNotBlank() },
                status = parseStatus(o.optString("status", TaskStatus.TODO.name)),
                deadline = if (o.isNull("deadline")) null else o.optLong("deadline"),
                isCompleted = o.optBoolean("isCompleted"),
                assigneeId = o.optString("assigneeId").takeIf { it.isNotBlank() },
                labels = labels,
                updatedAt = o.optLong("updatedAt")
            )
        }

    private fun parseStatus(value: String): TaskStatus {
        return try {
            TaskStatus.valueOf(value)
        } catch (_: Exception) {
            TaskStatus.TODO
        }
    }
}
