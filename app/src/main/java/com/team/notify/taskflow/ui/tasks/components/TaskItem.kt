package com.team.notify.taskflow.ui.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import java.util.*

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit
) {
    val isOverdue by remember(task) {
        mutableStateOf(task.status != TaskStatus.DONE && task.dueDate.before(Date()))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)

                if (task.description.isNotEmpty()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isOverdue) {
                    Text(
                        "OVERDUE",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                StatusChip(task.status)
                Spacer(Modifier.height(8.dp))
                QuickStatusButton(task.status, onStatusChange)
            }
        }
    }
}

@Composable
private fun QuickStatusButton(
    current: TaskStatus,
    onStatusChange: (TaskStatus) -> Unit
) {
    val next = when (current) {
        TaskStatus.TODO -> TaskStatus.DOING
        TaskStatus.DOING -> TaskStatus.DONE
        TaskStatus.DONE -> TaskStatus.TODO
    }

    TextButton(onClick = { onStatusChange(next) }) {
        Text("→ ${next.name}")
    }
}
