package com.team.notify.taskflow.data

import android.util.Log
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object DemoSeeder {
    suspend fun seed(db: AppDatabase) = withContext(Dispatchers.IO) {

        val spaceId = "default-space"
        val now = System.currentTimeMillis()

        val pageCount = runCatching { db.pageDao().countForSpace(spaceId) }.getOrDefault(0)
        val taskCount = runCatching { db.taskDao().countForSpace(spaceId) }.getOrDefault(0)

        Log.d("DEMO_SEED", "START pages=$pageCount tasks=$taskCount")

        if (pageCount > 0 || taskCount > 0) {
            Log.d("DEMO_SEED", "SKIP (already seeded)")
            return@withContext
        }

        db.spaceDao().insert(
            SpaceEntity(
                id = spaceId,
                name = "Notify Workspace",
                description = "Demo workspace for Role B & C testing",
                createdAt = now,
                updatedAt = now
            )
        )

        val welcomePage = PageEntity(
            id = "page-1",
            spaceId = spaceId,
            title = "Welcome",
            content = """
# Welcome 👋
This is a demo page.

Checklist:
- [ ] Create a new page
- [ ] Create a task
- [ ] Assign a task
- [ ] Set a due date
- [ ] Try offline, then reopen app
            """.trimIndent()
        )

        val meetingPage = PageEntity(
            id = "page-2",
            spaceId = spaceId,
            title = "Meeting Notes",
            content = """
## Agenda
- Progress review
- Next tasks
- Risks

## Action items
- [ ] Prepare slides
- [ ] Fix sync bugs
- [ ] Test reminders
            """.trimIndent()
        )

        db.pageDao().insert(welcomePage)
        db.pageDao().insert(meetingPage)

        val futureDueSoon = now + 60 * 60 * 1000L
        val futureDueLater = now + 24 * 60 * 60 * 1000L
        val overdueDue = now - 2 * 60 * 60 * 1000L

        val tasks = listOf(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                pageId = welcomePage.id,
                title = "Explore the UI",
                description = "Open pages, open tasks, try Notion-style layout",
                status = TaskStatus.TODO,
                isCompleted = false,
                assigneeId = "u2",
                deadline = futureDueLater,
                labels = listOf("ui", "demo"),
                updatedAt = now
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                pageId = meetingPage.id,
                title = "Working task (Doing)",
                description = "This task is in DOING status for filter testing",
                status = TaskStatus.DOING,
                isCompleted = false,
                assigneeId = "u1",
                deadline = futureDueSoon,
                labels = listOf("work", "roleB"),
                updatedAt = now
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                pageId = meetingPage.id,
                title = "Overdue task",
                description = "This one is overdue → should show overdue badge",
                status = TaskStatus.TODO,
                isCompleted = false,
                assigneeId = "u3",
                deadline = overdueDue,
                labels = listOf("urgent", "test"),
                updatedAt = now
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                pageId = welcomePage.id,
                title = "Completed example",
                description = "Done task for Done filter testing",
                status = TaskStatus.DONE,
                isCompleted = true,
                assigneeId = "u2",
                deadline = null,
                labels = listOf("done"),
                updatedAt = now
            )
        )

        tasks.forEach { db.taskDao().insert(it) }

        val pc2 = db.pageDao().countForSpace(spaceId)
        val tc2 = db.taskDao().countForSpace(spaceId)
        Log.d("DEMO_SEED", "DONE pages=$pc2 tasks=$tc2")
    }
}
