package com.team.notify.taskflow.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "newRowScale"
    )
    
    val backgroundColor = if (isPressed) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Row(
        Modifier
            .fillMaxWidth()
            .notionClickable(onClick)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .padding(vertical = 14.dp, horizontal = 12.dp)
            .scale(scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "➕",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
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
            .notionClickable(onOpen)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { 
                    onToggleDone()
                },
                modifier = Modifier.scale(1.1f)
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    task.title.ifBlank { "Untitled task" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (task.isCompleted) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                val desc = task.description?.trim().orEmpty()
                if (desc.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Add metadata row
                if (task.deadline != null || task.assigneeId != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.deadline?.let { deadline ->
                            Text(
                                "📅 ${formatDate(deadline)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        task.assigneeId?.let { assigneeId ->
                            Text(
                                "👤 $assigneeId",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        StatusBadge(task.status)
    }
}

private fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val (label, color, icon) = when (status) {
        TaskStatus.TODO -> Triple(
            "Todo", 
            MaterialTheme.colorScheme.surfaceVariant,
            "📝"
        )
        TaskStatus.DOING -> Triple(
            "Doing", 
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            "⚡"
        )
        TaskStatus.DONE -> Triple(
            "Done", 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            "✅"
        )
    }

    AssistChip(
        onClick = {},
        label = { 
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon)
                Text(label)
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color
        )
    )
}
