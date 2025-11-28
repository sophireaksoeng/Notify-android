package com.team.notify.taskflow.ui.tasks.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.team.notify.taskflow.model.TaskStatus

@Composable
fun StatusChip(status: TaskStatus) {
    val (label, container, content) = when (status) {
        TaskStatus.TODO -> Triple("TODO", Color(0xFFEEEFF7), Color(0xFF1E3A8A))
        TaskStatus.DOING -> Triple("DOING", Color(0xFFE0F2FE), Color(0xFF075985))
        TaskStatus.DONE -> Triple("DONE", Color(0xFFE6F4EA), Color(0xFF14532D))
    }
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        enabled = false,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content,
            disabledContainerColor = container,
            disabledLabelColor = content
        )
    )
}
