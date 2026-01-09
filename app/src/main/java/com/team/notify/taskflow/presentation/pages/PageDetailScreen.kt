package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.notify.taskflow.domain.model.Block

@Composable
fun PageDetailScreen(
    pageId: String?,
    onNavigateBack: () -> Unit,
    viewModel: PageDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.saving.collectAsStateWithLifecycle()

    // Load data once when entering composition
    LaunchedEffect(pageId) {
        viewModel.load(pageId)
    }

    Scaffold(
        topBar = {
            PageDetailTopBar(
                isSaving = isSaving,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        PageContent(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            title = uiState.title,
            content = uiState.content,
            onTitleChange = viewModel::updateTitle,
            onContentChange = { newText ->
                // For a simple text editor, we treat the whole content as one update
                viewModel.updateContent(newText)

                // If you have a complex block editor, you would construct blocks here:
                // viewModel.onBlocksChanged(parseTextToBlocks(newText))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetailTopBar(
    isSaving: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { }, // Title is in the content area like Notion/Google Docs
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (isSaving) {
                Text(
                    text = "Saving...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 16.dp)
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    text = "Saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    )
}

@Composable
fun PageContent(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp) // Add nice side padding
            .fillMaxSize()
    ) {
        // 1. Title Input (Large, Bold)
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.padding(vertical = 16.dp)) {
                    if (title.isEmpty()) {
                        Text(
                            text = "Untitled",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Content Body
        // Note: For a real block editor, replace this BasicTextField with a LazyColumn of blocks
        BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { innerTextField ->
                Box {
                    if (content.isEmpty()) {
                        Text(
                            text = "Type something...",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}