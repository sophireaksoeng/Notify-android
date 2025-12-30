package com.team.notify.taskflow.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NotionPageRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .notionClickable(onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Spacer(Modifier.height(10.dp))
        NotionDivider()
    }
}

@Composable
fun NotionTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Untitled"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        textStyle = MaterialTheme.typography.headlineLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun NotionBodyField(
    value: String?,
    onValueChange: (String) -> Unit,
    placeholder: String = "Start writing…",
    minLines: Int = 6
) {
    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        textStyle = MaterialTheme.typography.bodyLarge,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}