package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageContentEditor(
    content: String,
    onContentChange: (String) -> Unit,
    enabled: Boolean = true
) {
    var tab by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }) { Text("Edit") }
            Tab(selected = tab == 1, onClick = { tab = 1 }) { Text("Preview") }
        }

        if (tab == 0) {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                label = { Text("Markdown Content") },
                maxLines = Int.MAX_VALUE,
                enabled = enabled,
                singleLine = false,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Markdown(
                    content = content
                )
            }
        }
    }
}
