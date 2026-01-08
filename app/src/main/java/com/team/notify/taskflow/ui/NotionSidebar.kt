package com.team.notify.taskflow.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.PageEntity

@Composable
fun NotionSidebar(
    title: String = "Workspace",
    pages: List<PageEntity>,
    selectedPageId: String?,
    onSelectPage: (String) -> Unit,
    onNewPage: () -> Unit = {}, // Keep for compatibility but won't be used
    onRenamePage: (String, String) -> Unit = { _, _ -> },
    onDeletePage: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var pageToRename by remember { mutableStateOf<PageEntity?>(null) }

    val filtered = remember(pages, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) pages
        else pages.filter { it.title.lowercase().contains(q) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {

        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search pages…") }
        )

        Spacer(Modifier.height(12.dp))
        NotionDivider()
        Spacer(Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Text(
                "No pages yet.\nCreate your first page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            return
        }

        filtered.forEach { p ->
            val selected = p.id == selectedPageId
            SidebarItem(
                emoji = "📄",
                title = p.title.ifBlank { "Untitled" },
                selected = selected,
                onClick = { onSelectPage(p.id) },
                onRename = { 
                    pageToRename = p
                    showRenameDialog = true
                },
                onDelete = { onDeletePage(p.id) }
            )
        }
        
        // Rename dialog
        if (showRenameDialog && pageToRename != null) {
            var newName by remember { mutableStateOf(pageToRename?.title ?: "") }
            
            AlertDialog(
                onDismissRequest = { 
                    showRenameDialog = false
                    pageToRename = null
                },
                title = { Text("Rename Page") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Page name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pageToRename?.let { page ->
                                onRenamePage(page.id, newName)
                            }
                            showRenameDialog = false
                            pageToRename = null
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showRenameDialog = false
                        pageToRename = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SidebarItem(
    emoji: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val bg = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        Color.Transparent
    }

    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        // Action buttons
        Row {
            IconButton(
                onClick = onRename,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Rename",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black,
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black,
                )
            }
        }
    }
}
