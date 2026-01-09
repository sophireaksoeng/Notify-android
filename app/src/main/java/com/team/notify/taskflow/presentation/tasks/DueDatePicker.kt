package com.team.notify.taskflow.presentation.tasks

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDatePicker(
    isOpen: Boolean,
    initialDate: Long? = null,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    if (isOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    onDismiss()
                }) {
                    Text("Clear")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun Long.toFormattedDate(): String {
    val localDate = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
    return localDate.format(formatter)
}