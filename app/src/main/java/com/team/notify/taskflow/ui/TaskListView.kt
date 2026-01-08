package com.team.notify.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListView(
    tasks: List<TaskEntity>,
    onTaskClick: (String) -> Unit,
    onAddTask: (String) -> Unit = { },
    onRenameTask: (String, String) -> Unit = { _, _ -> },
    onDeleteTask: (String) -> Unit = {},
    onToggleComplete: (String) -> Unit = {},
    onSetStatus: (String, TaskStatus) -> Unit = { _, _ -> },
    onNavigateToProfile: () -> Unit = {}
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var taskToRename by remember { mutableStateOf<TaskEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filteredTasks by remember { derivedStateOf { tasks.filter { it.title.contains(query, ignoreCase = true) } } }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header - SpacesScreen style (no shadow) with Add button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "${tasks.count { it.isCompleted }} of ${tasks.size} completed (${if (tasks.isNotEmpty()) (tasks.count { it.isCompleted } * 100 / tasks.size) else 0}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                // Add Task Button at the top
                IconButton(
                    onClick = { 
                        println("TaskListView: Add Task button clicked")
                        showCreateDialog = true 
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF4A90E2),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Content area - SpacesScreen style
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Search field (no shadow)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color.White
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search tasks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        cursorColor = Color(0xFF1F2937),
                        focusedTextColor = Color(0xFF1F2937),
                        unfocusedTextColor = Color(0xFF1F2937)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (filteredTasks.isEmpty()) {
                // Empty state - SpacesScreen style (no shadow)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Color(0xFF4A90E2).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📋",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 32.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "No Tasks Yet",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1F2937)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Create your first task to start organizing your work",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Task list - SpacesScreen style
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onLongClick = {
                                taskToRename = task
                                showRenameDialog = true
                            },
                            onToggleComplete = { onToggleComplete(task.id) },
                            onSetStatus = { status -> onSetStatus(task.id, status) }
                        )
                    }
                }
            }
        }
    }

    // Create Task Dialog
    if (showCreateDialog) {
        println("TaskListView: Create Task dialog is showing")
        var taskTitle by remember { mutableStateOf("") }
        var taskDescription by remember { mutableStateOf("") }
        var taskPriority by remember { mutableStateOf("Medium") }
        
        AlertDialog(
            onDismissRequest = { 
                println("TaskListView: Dialog dismissed")
                showCreateDialog = false 
            },
            title = { 
                Text(
                    "Create New Task",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Task Name
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Name *") },
                        placeholder = { Text("Enter task name...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    
                    // Task Description
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Description") },
                        placeholder = { Text("Enter task description...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    
                    // Priority Selection
                    Column {
                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF374151)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Low", "Medium", "High").forEach { priority ->
                                FilterChip(
                                    onClick = { taskPriority = priority },
                                    label = { 
                                        Text(
                                            priority,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        ) 
                                    },
                                    selected = taskPriority == priority,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (priority) {
                                            "High" -> Color.Red
                                            "Medium" -> Color(0xFFD97706)
                                            "Low" -> Color.Green
                                            else -> Color(0xFF4A90E2)
                                        },
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        println("TaskListView: Create Task button clicked, title: '$taskTitle'")
                        if (taskTitle.isNotBlank()) {
                            // Create task with clean title (the backend will handle priority and description)
                            println("TaskListView: Calling onAddTask with: '${taskTitle.trim()}'")
                            onAddTask(taskTitle.trim())
                            showCreateDialog = false
                        } else {
                            println("TaskListView: Task title is blank, not creating")
                        }
                    },
                    enabled = taskTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        contentColor = Color.White
                    )
                ) {
                    Text("Create Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Rename Dialog
    if (showRenameDialog && taskToRename != null) {
        var newName by remember { mutableStateOf(taskToRename?.title ?: "") }
        
        AlertDialog(
            onDismissRequest = { 
                showRenameDialog = false
                taskToRename = null
            },
            title = { Text("Rename Task") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Task name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToRename?.let { task ->
                            onRenameTask(task.id, newName)
                        }
                        showRenameDialog = false
                        taskToRename = null
                    },
                    enabled = newName.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRenameDialog = false
                    taskToRename = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleComplete: () -> Unit = {},
    onSetStatus: (TaskStatus) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Priority indicator
                        when (task.priority.lowercase()) {
                            "high" -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            Color.Red,
                                            CircleShape
                                )
                                )
                            }
                            "medium" -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            Color.Yellow,
                                            CircleShape
                                )
                                )
                            }
                            "low" -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            Color.Green,
                                            CircleShape
                                )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Status/Complete indicator
                        IconButton(
                            onClick = onToggleComplete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (task.isCompleted) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (task.isCompleted) "Completed" else "Not completed",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = task.title.ifBlank { "Untitled" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold
                                ),
                                color = if (task.isCompleted) 
                                    Color(0xFF6B7280)
                                else 
                                    Color(0xFF1F2937),
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (!task.description.isNullOrBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                
                IconButton(
                    onClick = { /* TODO: Quick actions */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TaskStatItem(
                    icon = Icons.Default.Flag,
                    label = "Priority",
                    value = task.priority.ifBlank { "Medium" },
                    color = when (task.priority.lowercase()) {
                        "high" -> Color.Red
                        "medium" -> Color.Yellow
                        "low" -> Color.Green
                        else -> Color(0xFF6B7280)
                    }
                )
                
                TaskStatItem(
                    icon = Icons.Default.Schedule,
                    label = "Due",
                    value = task.deadline?.let { deadline ->
                        val now = System.currentTimeMillis()
                        val daysUntilDeadline = (deadline - now) / (24 * 60 * 60 * 1000)
                        when {
                            daysUntilDeadline < 0 -> "Overdue"
                            daysUntilDeadline == 0L -> "Today"
                            daysUntilDeadline == 1L -> "Tomorrow"
                            daysUntilDeadline <= 7L -> "In ${daysUntilDeadline}d"
                            else -> "In ${daysUntilDeadline}d"
                        }
                    } ?: "No due date",
                    color = Color(0xFF4A90E2)
                )
                
                TaskStatItem(
                    icon = Icons.Default.Info,
                    label = "Status",
                    value = when (task.status) {
                        TaskStatus.TODO -> "To Do"
                        TaskStatus.DOING -> "In Progress"
                        TaskStatus.DONE -> "Done"
                    },
                    color = Color(0xFFF59E0B)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF4A90E2)
                    )
                ) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                IconButton(
                    onClick = { /* TODO: Quick actions */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF4A90E2).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF1F2937)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun TaskListItem(
    task: TaskEntity,
    onClick: () -> Unit,
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleComplete: () -> Unit = {},
    onSetStatus: (TaskStatus) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Priority indicator
                when (task.priority.lowercase()) {
                    "high" -> {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    Color.Red,
                                    CircleShape
                                )
                        )
                    }
                    "medium" -> {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    Color.Yellow,
                                    CircleShape
                                )
                        )
                    }
                    "low" -> {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    Color.Green,
                                    CircleShape
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(6.dp))
                
                // Status/Complete indicator
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (task.isCompleted) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (task.isCompleted) "Completed" else "Not completed",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                        ),
                        color = if (task.isCompleted) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Status badge and deadline in same row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status badge
                        StatusBadge(status = task.status)
                        
                        // Deadline indicator
                        task.deadline?.let { deadline ->
                            val now = System.currentTimeMillis()
                            val daysUntilDeadline = (deadline - now) / (24 * 60 * 60 * 1000)
                            
                            val deadlineText = when {
                                daysUntilDeadline < 0 -> "Overdue"
                                daysUntilDeadline == 0L -> "Due today"
                                daysUntilDeadline == 1L -> "Due tomorrow"
                                daysUntilDeadline <= 7L -> "Due in ${daysUntilDeadline}d"
                                else -> "Due in ${daysUntilDeadline}d"
                            }
                            
                            val deadlineColor = when {
                                daysUntilDeadline < 0 -> Color.Red
                                daysUntilDeadline == 0L -> Color(0xFFFFA500) // Orange
                                daysUntilDeadline == 1L -> Color.Yellow
                                daysUntilDeadline <= 7L -> Color.Blue
                                else -> Color.Gray
                            }
                            
                            Text(
                                text = deadlineText,
                                style = MaterialTheme.typography.bodySmall,
                                color = deadlineColor
                            )
                        }
                    }
                }
            }
            
            // Action buttons
            Row {
                // Status dropdown
                var expanded by remember { mutableStateOf(false) }
                
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TaskStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name.replace("_", " "), fontSize = 14.sp) },
                            onClick = {
                                onSetStatus(status)
                                expanded = false
                            }
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val (color, text) = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.outline to "To Do"
        TaskStatus.DOING -> MaterialTheme.colorScheme.primary to "Doing"
        TaskStatus.DONE -> MaterialTheme.colorScheme.primary to "Done"
    }
    
    Surface(
        modifier = Modifier.padding(top = 4.dp),
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
