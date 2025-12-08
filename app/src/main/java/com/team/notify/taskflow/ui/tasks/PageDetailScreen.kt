package com.team.notify.taskflow.ui.tasks

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
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.presentation.pages.PageDetailViewModel

@Composable
fun PageDetailScreen(
    pageId: String?,
    spaceId: String,
    viewModel: PageDetailViewModel,
    onSaved: () -> Unit,
) {
    LaunchedEffect(pageId) {
        viewModel.load(pageId)
    }

    val state by viewModel.uiState.collectAsState()

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Page Title") }
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.description ?: "",
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Description") }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.save(spaceId)
                onSaved()
            }
        ) {
            Text("Save")
        }
    }
}