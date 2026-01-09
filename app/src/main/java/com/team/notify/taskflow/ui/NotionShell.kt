package com.team.notify.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.model.PageType

@Composable
fun NotionShell(
    sidebar: @Composable () -> Unit,
    content: @Composable () -> Unit,
    phoneList: (@Composable () -> Unit)? = null,
    showPhoneList: Boolean = false,
    onSync: (() -> Unit)? = null,
    onAdd: ((String, PageType) -> Unit)? = null
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            Row(Modifier.fillMaxSize()) {
                Surface(
                    tonalElevation = 0.dp,
                    modifier = Modifier.width(320.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface
                ) { sidebar() }

                Surface(
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(Modifier.fillMaxSize()) {
                        content()
                        
                        // Floating Action Button for pages
                        onAdd?.let { add ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                FloatingActionButton(
                                    onClick = { showCreateDialog = true },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Page"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Mobile top bar with sync button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notify",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        onSync?.let { sync ->
                            IconButton(
                                onClick = sync,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = Color.Black,
                                )
                            }
                        }
                    }
                    
                    // Mobile content
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (showPhoneList && phoneList != null) phoneList() else content()
                        
                        // Floating Action Button for pages (mobile)
                        onAdd?.let { add ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                FloatingActionButton(
                                    onClick = { showCreateDialog = true },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Page"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Create Page Dialog
    if (showCreateDialog) {
        var pageTitle by remember { mutableStateOf("") }
        var selectedPageType by remember { mutableStateOf(PageType.TASKS) }
        
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Page") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pageTitle,
                        onValueChange = { pageTitle = it },
                        label = { Text("Page Title") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                    
                    Text(
                        text = "Page Type:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Page type selection
                    Column {
                        PageTypeOption(
                            type = PageType.TASKS,
                            title = "Tasks",
                            description = "Create and manage tasks with deadlines and priorities",
                            icon = "📋",
                            selected = selectedPageType == PageType.TASKS,
                            onClick = { selectedPageType = PageType.TASKS }
                        )
                        
                        PageTypeOption(
                            type = PageType.NOTES,
                            title = "Notes",
                            description = "Write and organize notes with rich text",
                            icon = "📝",
                            selected = selectedPageType == PageType.NOTES,
                            onClick = { selectedPageType = PageType.NOTES }
                        )
                        
                        PageTypeOption(
                            type = PageType.DOCUMENT,
                            title = "Document",
                            description = "Create structured documents with sections",
                            icon = "📄",
                            selected = selectedPageType == PageType.DOCUMENT,
                            onClick = { selectedPageType = PageType.DOCUMENT }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pageTitle.isNotBlank()) {
                            onAdd?.invoke(pageTitle.trim(), selectedPageType)
                            showCreateDialog = false
                        }
                    },
                    enabled = pageTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PageTypeOption(
    type: PageType,
    title: String,
    description: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (selected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
