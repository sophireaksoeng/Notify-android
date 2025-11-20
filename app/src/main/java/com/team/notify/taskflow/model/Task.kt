package com.team.notify.taskflow.model

import java.util.Date

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val dueDate: Date
)

enum class TaskStatus { TODO, DOING, DONE }