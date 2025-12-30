package com.team.notify.taskflow.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.presentation.theme.NotionStyle

@Composable
fun SaveStatus(isSaving: Boolean) {
    Text(
        if (isSaving) "Saving…" else "Saved",
        style = NotionStyle.SubtleText,
        modifier = Modifier.padding(8.dp)
    )
}
