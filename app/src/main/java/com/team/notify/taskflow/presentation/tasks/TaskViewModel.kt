package com.team.notify.taskflow.presentation.tasks

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import java.util.Date
import java.util.Calendar

class TaskViewModel : ViewModel() {

    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks

    init {
        _tasks.addAll(
            listOf(
                Task(
                    title = "Fix login bug",
                    description = "Resolve crash on login",
                    status = TaskStatus.TODO,
                    dueDate = Calendar.getInstance().apply { add(Calendar.HOUR, 6) }.time
                ),
                Task(
                    title = "UI polish",
                    description = "Improve animations",
                    status = TaskStatus.DOING,
                    dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time
                ),
                Task(
                    title = "Write ReminderWorker",
                    description = "Set up WorkManager",
                    status = TaskStatus.DONE,
                    dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.time
                )
            )
        )
    }
}

