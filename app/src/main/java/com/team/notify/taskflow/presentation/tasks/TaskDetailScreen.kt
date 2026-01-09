package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var showDatePicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    
    // Format current deadline for display
    val currentDeadlineText = current?.let { deadline ->
        val now = System.currentTimeMillis()
        val daysUntilDeadline = (deadline - now) / (24 * 60 * 60 * 1000)
        when {
            daysUntilDeadline < 0 -> "Overdue"
            daysUntilDeadline == 0L -> "Today"
            daysUntilDeadline == 1L -> "Tomorrow"
            daysUntilDeadline <= 7L -> "In ${daysUntilDeadline}d"
            else -> java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(deadline))
        }
    } ?: "No due date"
    
    Column {
        // Current deadline display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentDeadlineText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (current != null) {
                        val now = System.currentTimeMillis()
                        val daysUntilDeadline = (current - now) / (24 * 60 * 60 * 1000)
                        when {
                            daysUntilDeadline < 0 -> Color.Red
                            daysUntilDeadline == 0L -> Color(0xFFEA580C)
                            daysUntilDeadline <= 3L -> Color(0xFFD97706)
                            else -> Color(0xFF059669)
                        }
                    } else Color(0xFF6B7280)
                )
            )
            
            TextButton(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4F46E5)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pick Date")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Quick selection buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                onClick = { onChange(null) },
                label = { 
                    Text(
                        "None",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                selected = current == null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4F46E5),
                    selectedLabelColor = Color.White
                )
            )
            
            FilterChip(
                onClick = { 
                    val tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                    onChange(tomorrow) 
                },
                label = { 
                    Text(
                        "Today",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                selected = isToday(current),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4F46E5),
                    selectedLabelColor = Color.White
                )
            )
            
            FilterChip(
                onClick = { 
                    val tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                    onChange(tomorrow) 
                },
                label = { 
                    Text(
                        "Tomorrow",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                selected = isTomorrow(current),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4F46E5),
                    selectedLabelColor = Color.White
                )
            )
            
            FilterChip(
                onClick = { 
                    val nextWeek = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
                    onChange(nextWeek) 
                },
                label = { 
                    Text(
                        "Next Week",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                selected = isNextWeek(current),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4F46E5),
                    selectedLabelColor = Color.White
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Additional quick options
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                onClick = { 
                    val in3Days = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000
                    onChange(in3Days) 
                },
                label = { 
                    Text(
                        "In 3 Days",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                selected = isIn3Days(current),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4F46E5),
                    selectedLabelColor = Color.White
                )
            )
            
            FilterChip(
                onClick = { 
                    val in2Weeks = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000
                    onChange(in2Weeks) 
                },
                label = { 
                    Text(
                        "In 2 Weeks",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    ) 
                },
                selected = isIn2Weeks(current),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4F46E5),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDatePicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Select Due Date",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF111827)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple date selection buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Today" to System.currentTimeMillis(),
                            "Tomorrow" to System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                            "In 3 Days" to System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000,
                            "Next Week" to System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000,
                            "In 2 Weeks" to System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000
                        ).forEach { (label, time) ->
                            Button(
                                onClick = {
                                    onChange(time)
                                    showDatePicker = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF3F4F6),
                                    contentColor = Color(0xFF111827)
                                )
                            ) {
                                Text(label)
                            }
                        }
                        
                        Button(
                            onClick = {
                                onChange(null)
                                showDatePicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF6B7280)
                            )
                        ) {
                            Text("Remove Due Date")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

// Helper functions to check date matches
private fun isToday(deadline: Long?): Boolean {
    if (deadline == null) return false
    val now = System.currentTimeMillis()
    val today = java.util.Calendar.getInstance()
    val deadlineCal = java.util.Calendar.getInstance().apply { timeInMillis = deadline }
    return today.get(java.util.Calendar.YEAR) == deadlineCal.get(java.util.Calendar.YEAR) &&
           today.get(java.util.Calendar.DAY_OF_YEAR) == deadlineCal.get(java.util.Calendar.DAY_OF_YEAR)
}

private fun isTomorrow(deadline: Long?): Boolean {
    if (deadline == null) return false
    val tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000
    val tomorrowCal = java.util.Calendar.getInstance().apply { timeInMillis = tomorrow }
    val deadlineCal = java.util.Calendar.getInstance().apply { timeInMillis = deadline }
    return tomorrowCal.get(java.util.Calendar.YEAR) == deadlineCal.get(java.util.Calendar.YEAR) &&
           tomorrowCal.get(java.util.Calendar.DAY_OF_YEAR) == deadlineCal.get(java.util.Calendar.DAY_OF_YEAR)
}

private fun isNextWeek(deadline: Long?): Boolean {
    if (deadline == null) return false
    val nextWeek = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
    val nextWeekCal = java.util.Calendar.getInstance().apply { timeInMillis = nextWeek }
    val deadlineCal = java.util.Calendar.getInstance().apply { timeInMillis = deadline }
    return nextWeekCal.get(java.util.Calendar.YEAR) == deadlineCal.get(java.util.Calendar.YEAR) &&
           nextWeekCal.get(java.util.Calendar.DAY_OF_YEAR) == deadlineCal.get(java.util.Calendar.DAY_OF_YEAR)
}

private fun isIn3Days(deadline: Long?): Boolean {
    if (deadline == null) return false
    val in3Days = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000
    val in3DaysCal = java.util.Calendar.getInstance().apply { timeInMillis = in3Days }
    val deadlineCal = java.util.Calendar.getInstance().apply { timeInMillis = deadline }
    return in3DaysCal.get(java.util.Calendar.YEAR) == deadlineCal.get(java.util.Calendar.YEAR) &&
           in3DaysCal.get(java.util.Calendar.DAY_OF_YEAR) == deadlineCal.get(java.util.Calendar.DAY_OF_YEAR)
}

private fun isIn2Weeks(deadline: Long?): Boolean {
    if (deadline == null) return false
    val in2Weeks = System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000
    val in2WeeksCal = java.util.Calendar.getInstance().apply { timeInMillis = in2Weeks }
    val deadlineCal = java.util.Calendar.getInstance().apply { timeInMillis = deadline }
    return in2WeeksCal.get(java.util.Calendar.YEAR) == deadlineCal.get(java.util.Calendar.YEAR) &&
           in2WeeksCal.get(java.util.Calendar.DAY_OF_YEAR) == deadlineCal.get(java.util.Calendar.DAY_OF_YEAR)
}
