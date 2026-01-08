package com.team.notify.taskflow.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesDialog(
    preferences: UserPreferences,
    onDismiss: () -> Unit,
    onSave: (UserPreferences) -> Unit
) {
    var currentPreferences by remember { mutableStateOf(preferences) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preferences") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Selection
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    listOf("system", "light", "dark").forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentPreferences.theme == theme,
                                    onClick = { currentPreferences = currentPreferences.copy(theme = theme) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentPreferences.theme == theme,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Notifications
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                SwitchPreference(
                    title = "Enable Notifications",
                    subtitle = "Allow app to send notifications",
                    checked = currentPreferences.notificationsEnabled,
                    onCheckedChange = { currentPreferences = currentPreferences.copy(notificationsEnabled = it) }
                )

                SwitchPreference(
                    title = "Email Notifications",
                    subtitle = "Receive email notifications",
                    checked = currentPreferences.emailNotifications,
                    onCheckedChange = { currentPreferences = currentPreferences.copy(emailNotifications = it) }
                )

                SwitchPreference(
                    title = "Push Notifications",
                    subtitle = "Receive push notifications",
                    checked = currentPreferences.pushNotifications,
                    onCheckedChange = { currentPreferences = currentPreferences.copy(pushNotifications = it) }
                )

                // Sync
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                SwitchPreference(
                    title = "Auto Sync",
                    subtitle = "Automatically sync data",
                    checked = currentPreferences.autoSync,
                    onCheckedChange = { currentPreferences = currentPreferences.copy(autoSync = it) }
                )

                // Date & Time
                Text(
                    text = "Date & Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                // Date Format
                Text(
                    text = "Date Format",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    listOf("MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd").forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentPreferences.dateFormat == format,
                                    onClick = { currentPreferences = currentPreferences.copy(dateFormat = format) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentPreferences.dateFormat == format,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = format,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Time Format
                Text(
                    text = "Time Format",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    listOf("h:mm a", "HH:mm", "24-hour").forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentPreferences.timeFormat == format,
                                    onClick = { currentPreferences = currentPreferences.copy(timeFormat = format) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentPreferences.timeFormat == format,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = format,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(currentPreferences) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SwitchPreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
