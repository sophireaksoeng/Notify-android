package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.presentation.common.ConflictBadge

@Composable
fun PageDetailScreen(
    pageId: String?,
    spaceId: String,
    viewModel: PageDetailViewModel,
    onSaved: () -> Unit,
    canEdit: Boolean
) {
    LaunchedEffect(pageId) {
        viewModel.load(pageId)
    }

    val state by viewModel.uiState.collectAsState()

    if (state.hasConflict) {
        ConflictBadge()
        Spacer(Modifier.height(8.dp))
        Text(
            "This page was edited on another device.",
            color = Color.Red
        )
        Spacer(Modifier.height(8.dp))
    }

    Column(Modifier.padding(16.dp)) {
        if (!canEdit) {
            Text("Read-only mode", color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Page Title") },
            enabled = canEdit
        )

        Spacer(Modifier.height(12.dp))

        PageContentEditor(
            content = state.content,
            onContentChange = { viewModel.updateContent(it) },
            enabled = canEdit
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.save(spaceId)
                onSaved()
            },
            enabled = canEdit
        ) {
            Text("Save")
        }
    }
}
