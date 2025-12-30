package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus
import com.team.notify.taskflow.ui.NotionBodyField
import com.team.notify.taskflow.ui.NotionDivider
import com.team.notify.taskflow.ui.NotionTitleField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: TaskEntity,
    members: List<SpaceMemberEntity>,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSetStatus: (TaskStatus) -> Unit,
    onAssigneeChange: (String?) -> Unit,
    onDeadlineChange: (Long?) -> Unit,
    onToggleCompleted: () -> Unit
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var desc by remember(task.id) { mutableStateOf(task.description ?: "") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDelete) { Text("Delete") }
        }

        Spacer(Modifier.height(8.dp))

        NotionTitleField(
            value = title,
            onValueChange = {
                title = it
                onTitleChange(it)
            },
            placeholder = "Untitled task"
        )

        Spacer(Modifier.height(8.dp))
        NotionDivider()
        Spacer(Modifier.height(10.dp))

        PropertyRow("Status") {
            StatusDropdown(selected = task.status, onChange = onSetStatus)
        }

        PropertyRow("Assignee") {
            AssigneeDropdown(
                members = members,
                selectedUserId = task.assigneeId,
                onChange = onAssigneeChange
            )
        }

        PropertyRow("Due") {
            DueQuickButtons(
                current = task.deadline,
                onChange = onDeadlineChange
            )
        }

        PropertyRow("Done") {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleCompleted() })
                Spacer(Modifier.width(6.dp))
                Text(if (task.isCompleted) "Completed" else "Not completed")
            }
        }

        Spacer(Modifier.height(10.dp))
        NotionDivider()
        Spacer(Modifier.height(10.dp))

        NotionBodyField(
            value = desc,
            onValueChange = {
                desc = it
                onDescriptionChange(it)
            },
            placeholder = "Add a description…",
            minLines = 8
        )
    }
}

@Composable
private fun PropertyRow(label: String, content: @Composable () -> Unit) {
    Row(
        Modifier.padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.width(90.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selected: TaskStatus,
    onChange: (TaskStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text(" ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskStatus.values().forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.name) },
                    onClick = { onChange(s); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssigneeDropdown(
    members: List<SpaceMemberEntity>,
    selectedUserId: String?,
    onChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = members.firstOrNull { it.userId == selectedUserId }?.userId ?: "Unassigned"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            label = { Text(" ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Unassigned") }, onClick = {
                onChange(null); expanded = false
            })
            members.forEach { m ->
                DropdownMenuItem(text = { Text(m.userId) }, onClick = {
                    onChange(m.userId); expanded = false
                })
            }
        }
    }
}

@Composable
private fun DueQuickButtons(
    current: Long?,
    onChange: (Long?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = { onChange(null) }, label = { Text("None") })
        AssistChip(
            onClick = { onChange(System.currentTimeMillis() + 24 * 60 * 60 * 1000) },
            label = { Text("Tomorrow") }
        )
        AssistChip(
            onClick = { onChange(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) },
            label = { Text("Next week") }
        )
    }
}
