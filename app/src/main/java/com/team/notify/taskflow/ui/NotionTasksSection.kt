package com.team.notify.taskflow.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus

@Composable
fun NotionTasksSection(
    title: String = "Tasks",
    tasks: List<TaskEntity>,
    onToggleDone: (TaskEntity) -> Unit,
    onOpenTask: (TaskEntity) -> Unit,
    onAddTask: () -> Unit
) {
    Spacer(Modifier.height(14.dp))
    NotionDivider()
    Spacer(Modifier.height(10.dp))

    Text(title, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))

    NewRow(text = "+ New task", onClick = onAddTask)
    Spacer(Modifier.height(6.dp))
    NotionDivider()
    Spacer(Modifier.height(8.dp))

    if (tasks.isEmpty()) {
        Text(
            "No tasks yet. Create one above.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        return
    }

    tasks.forEach { t ->
        TaskRowNotion(
            task = t,
            onToggleDone = { onToggleDone(t) },
            onOpen = { onOpenTask(t) }
        )
        NotionDivider()
    }
}

@Composable
private fun NewRow(
    text: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .notionClickable(onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun TaskRowNotion(
    task: TaskEntity,
    onToggleDone: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.weight(1f)) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleDone() }
            )
            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    task.title.ifBlank { "Untitled task" },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val desc = task.description?.trim().orEmpty()
                if (desc.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        StatusBadge(task.status)
    }
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val label = when (status) {
        TaskStatus.TODO -> "Todo"
        TaskStatus.DOING -> "Doing"
        TaskStatus.DONE -> "Done"
    }

    AssistChip(
        onClick = {},
        label = { Text(label) }
    )
}
