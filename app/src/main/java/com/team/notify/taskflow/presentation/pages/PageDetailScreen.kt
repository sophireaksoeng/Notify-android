package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.ui.NotionBodyField
import com.team.notify.taskflow.ui.NotionDivider
import com.team.notify.taskflow.ui.NotionPageHeader

@Composable
fun PageDetailScreen(
    pageId: String?,
    spaceId: String,
    onBack: () -> Unit,
    vm: PageDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val saving by vm.saving.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pageId) {
        vm.load(pageId)
    }

    if (pageId == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Select a page to start",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .widthIn(max = 900.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.weight(1f))
                if (saving) {
                    Text(
                        "Saving…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                TextButton(onClick = { vm.save(spaceId) }) { Text("Save") }
            }

            Spacer(Modifier.height(8.dp))
            NotionDivider()
            Spacer(Modifier.height(12.dp))

            NotionPageHeader(
                emoji = "📄",
                title = state.title,
                onTitleChange = { vm.updateTitle(it) }
            )

            Spacer(Modifier.height(12.dp))

            NotionBodyField(
                value = state.description,
                onValueChange = { vm.updateDescription(it) },
                placeholder = "Type / for blocks…",
                minLines = 12
            )
            
            // Add bottom padding to avoid FAB overlap
            Spacer(Modifier.height(80.dp))
        }
        
        // Floating Action Button for adding content
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF4A90E2),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Content",
                modifier = Modifier.size(24.dp)
            )
        }
    }
    
    // Add Content Dialog
    if (showAddDialog) {
        var contentType by remember { mutableStateOf("Text") }
        var contentTitle by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { 
                Text(
                    "Add Content Block",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Select content type:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Text", "Heading", "List", "Quote").forEach { type ->
                            FilterChip(
                                onClick = { contentType = type },
                                label = { 
                                    Text(
                                        type,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) 
                                },
                                selected = contentType == type,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4A90E2),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    
                    if (contentType == "Text") {
                        OutlinedTextField(
                            value = contentTitle,
                            onValueChange = { contentTitle = it },
                            label = { Text("Text content") },
                            placeholder = { Text("Enter text...") },
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
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Add content to page description
                        val newContent = when (contentType) {
                            "Text" -> if (contentTitle.isNotBlank()) contentTitle else "New text block"
                            "Heading" -> "## New Heading"
                            "List" -> "- New list item"
                            "Quote" -> "> New quote"
                            else -> "New content block"
                        }
                        vm.updateDescription(state.description + "\n\n" + newContent)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        contentColor = Color.White
                    )
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
