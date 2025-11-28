package com.team.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.ui.tasks.TaskDetailScreen
import com.team.notify.taskflow.ui.tasks.TaskListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)

        setContent {
            var selectedTask by remember { mutableStateOf<Task?>(null) }

            if (selectedTask == null) {
                TaskListScreen(onTaskSelected = { selectedTask = it })
            } else {
                TaskDetailScreen(
                    taskId = selectedTask!!.id,
                    onSaved = { selectedTask = null }
                )
            }
        }
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "reminders_channel",
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Task reminders" }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
