package com.team.notify.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.model.PageType
import com.team.notify.ui.components.NotifyLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListView(
    pages: List<PageEntity>,
    onPageClick: (String) -> Unit,
    onRenamePage: (String, String) -> Unit = { _, _ -> },
    onDeletePage: (String) -> Unit = {},
    onAddPage: (String, PageType) -> Unit = { _, _ -> },
    onNavigateToProfile: () -> Unit = {}
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var pageToRename by remember { mutableStateOf<PageEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filteredPages by remember { derivedStateOf { pages.filter { it.title.contains(query, ignoreCase = true) } } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header - SpacesList style with only profile icon
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Empty space (no logo)
                Spacer(modifier = Modifier.width(1.dp))
                
                // Right side - Profile icon only
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF4A90E2).copy(alpha = 0.1f),
                            CircleShape
                        )
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Search field - Clean, no shadow
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search pages") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A90E2),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF1F2937),
                    focusedTextColor = Color(0xFF1F2937),
                    unfocusedTextColor = Color(0xFF1F2937),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Content area
        if (filteredPages.isEmpty()) {
            // Empty state - Clean, no shadow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
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
                        text = "📄",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "No Pages Yet",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1F2937)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Create your first page to start organizing your content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Page list - Clean, no shadow
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPages, key = { it.id }) { page ->
                    PageItem(
                        page = page,
                        onClick = { onPageClick(page.id) },
                        onLongClick = {
                            pageToRename = page
                            showRenameDialog = true
                        }
                    )
                }
            }
        }
        
        // Floating Action Button for creating pages
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.padding(20.dp),
                containerColor = Color(0xFF4A90E2),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Page",
                    modifier = Modifier.size(24.dp)
                )
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
                            onAddPage(pageTitle.trim(), selectedPageType)
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

    // Rename Dialog
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
                    singleLine = true
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

@Composable
private fun PageItem(
    page: PageEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0F5) // Light pink background to test
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFF4A90E2).copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (page.pageType) {
                        PageType.TASKS -> "📋"
                        PageType.NOTES -> "📝"
                        PageType.DOCUMENT -> "📄"
                        else -> "📄"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Page info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = page.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when (page.pageType) {
                        PageType.TASKS -> "Tasks • Updated ${formatDate(page.updatedAt)}"
                        PageType.NOTES -> "Notes • Updated ${formatDate(page.updatedAt)}"
                        PageType.DOCUMENT -> "Document • Updated ${formatDate(page.updatedAt)}"
                        else -> "Page • Updated ${formatDate(page.updatedAt)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Action button
            IconButton(
                onClick = { /* TODO: Quick actions */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFF3F4F6),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PageListItem(
    page: PageEntity,
    onClick: () -> Unit,
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFF4A90E2).copy(alpha = 0.1f),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📄",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp
                            )
                        }
                        
                        Column {
                            Text(
                                text = page.title.ifBlank { "Untitled" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF1F2937),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Page",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = { /* TODO: Quick actions */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatItem(
                    icon = Icons.Default.Description,
                    label = "Type",
                    value = when (page.pageType) {
                        com.team.notify.taskflow.model.PageType.TASKS -> "Tasks"
                        com.team.notify.taskflow.model.PageType.NOTES -> "Notes"
                        com.team.notify.taskflow.model.PageType.DOCUMENT -> "Document"
                        else -> "Page"
                    },
                    color = Color(0xFF4A90E2)
                )
                
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Updated",
                    value = formatDate(page.updatedAt),
                    color = Color(0xFF10B981)
                )
                
                StatItem(
                    icon = Icons.Default.Info,
                    label = "Status",
                    value = "Active",
                    color = Color(0xFFF59E0B)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { /* TODO: Quick actions */ },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF4A90E2).copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(14.dp)
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

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> "long ago"
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        )
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
