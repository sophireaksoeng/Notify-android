package com.team.notify.taskflow.presentation.notes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPageScreen(
    pageId: String?,
    spaceId: String, // <--- FIXED: Added this parameter
    onBack: () -> Unit,
    vm: NotesPageViewModel = hiltViewModel()
) {
    val title by vm.title.collectAsState()
    val contentValue by vm.contentValue.collectAsState()
    val saving by vm.saving.collectAsState()
    val lastEdited by vm.lastEdited.collectAsState()

    // Pass both pageId and spaceId to the ViewModel
    LaunchedEffect(pageId, spaceId) {
        vm.load(pageId, spaceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    SaveStatusIndicator(saving)
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, "Options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            EditorToolbar(
                currentValue = contentValue,
                onApplyStyle = vm::applyStyle,
                onInsertList = vm::insertBulletPoint
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            BasicTextField(
                value = title,
                onValueChange = vm::updateTitle,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                "Untitled",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata
            Text(
                text = "Last edited $lastEdited",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Content
            BasicTextField(
                value = contentValue,
                onValueChange = vm::updateContentValue,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (contentValue.text.isEmpty()) {
                            Text(
                                "Type something...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(minHeight = 400.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SaveStatusIndicator(isSaving: Boolean) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 12.dp)) {
        AnimatedContent(targetState = isSaving, label = "SaveStatus") { saving ->
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Saved",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditorToolbar(
    currentValue: TextFieldValue,
    onApplyStyle: (SpanStyle) -> Unit,
    onInsertList: () -> Unit
) {
    val selection = currentValue.selection
    val styles = currentValue.annotatedString.spanStyles.filter {
        (it.start <= selection.start && it.end >= selection.end) ||
                (selection.collapsed && it.start <= selection.start && it.end >= selection.start)
    }

    val isBold = styles.any { it.item.fontWeight == FontWeight.Bold }
    val isItalic = styles.any { it.item.fontStyle == FontStyle.Italic }
    val isUnderlined = styles.any { it.item.textDecoration == TextDecoration.Underline }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .navigationBarsPadding()
                .imePadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EditorButton(Icons.Outlined.FormatBold, isBold) { onApplyStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
            EditorButton(Icons.Outlined.FormatItalic, isItalic) { onApplyStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
            EditorButton(Icons.Outlined.FormatUnderlined, isUnderlined) { onApplyStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
            EditorButton(Icons.AutoMirrored.Outlined.List, false, onInsertList)
            IconButton(onClick = { }) { Icon(Icons.Outlined.Image, "Image", tint = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

@Composable
fun EditorButton(icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    FilledIconToggleButton(
        checked = isActive,
        onCheckedChange = { onClick() },
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = Color.Transparent,
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(icon, contentDescription = null)
    }
}