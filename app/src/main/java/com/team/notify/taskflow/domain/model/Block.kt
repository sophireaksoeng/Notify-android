package com.team.notify.taskflow.domain.model

sealed class Block {
    abstract val id: String

    data class Text(
        override val id: String,
        val text: String
    ) : Block()

    data class Heading(
        override val id: String,
        val text: String
    ) : Block()

    data class Todo(
        override val id: String,
        val text: String,
        val checked: Boolean
    ) : Block()
}
