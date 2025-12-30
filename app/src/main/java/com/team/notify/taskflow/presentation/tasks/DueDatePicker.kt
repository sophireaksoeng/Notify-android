package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDatePicker(
    deadlineMillis: Long?,
    onChange: (Long?) -> Unit
) {
    var show by remember { mutableStateOf(false) }

    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val label = deadlineMillis?.let { fmt.format(Date(it)) } ?: "No due date"

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Due date") },
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = { show = true }) { Text("Set") }
        OutlinedButton(onClick = { onChange(null) }) { Text("Clear") }
    }

    if (show) {
        DueDateDialog(
            initialMillis = deadlineMillis,
            onDismiss = { show = false },
            onSave = { millis ->
                onChange(millis)
                show = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val now = System.currentTimeMillis()
    val startMillis = initialMillis ?: now

    val dateState = rememberDatePickerState(initialSelectedDateMillis = startMillis)

    var hour by remember { mutableIntStateOf(Calendar.getInstance().apply { timeInMillis = startMillis }.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(Calendar.getInstance().apply { timeInMillis = startMillis }.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val dateMillis = dateState.selectedDateMillis ?: now
                val cal = Calendar.getInstance().apply {
                    timeInMillis = dateMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onSave(cal.timeInMillis)
            }) { Text("Save") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Pick due date") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DatePicker(state = dateState)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HourDropdown(hour = hour, onChange = { hour = it }, modifier = Modifier.weight(1f))
                    MinuteDropdown(minute = minute, onChange = { minute = it }, modifier = Modifier.weight(1f))
                }
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourDropdown(hour: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = hour.toString().padStart(2, '0'),
            onValueChange = {},
            readOnly = true,
            label = { Text("Hour") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (0..23).forEach { h ->
                DropdownMenuItem(text = { Text(h.toString().padStart(2, '0')) }, onClick = {
                    onChange(h); expanded = false
                })
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MinuteDropdown(minute: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val minutes = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = minute.toString().padStart(2, '0'),
            onValueChange = {},
            readOnly = true,
            label = { Text("Minute") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            minutes.forEach { m ->
                DropdownMenuItem(text = { Text(m.toString().padStart(2, '0')) }, onClick = {
                    onChange(m); expanded = false
                })
            }
        }
    }
}
