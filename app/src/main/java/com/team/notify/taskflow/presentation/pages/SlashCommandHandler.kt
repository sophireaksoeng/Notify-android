package com.team.notify.taskflow.presentation.pages

import com.team.notify.taskflow.domain.model.Block
import java.util.UUID

fun handleSlashCommand(
    input: String,
    blocks: List<Block>,
    index: Int
): List<Block> {

    val newBlock = when (input) {
        "/todo" -> Block.Todo(UUID.randomUUID().toString(), "", false)
        "/heading" -> Block.Heading(UUID.randomUUID().toString(), "")
        else -> Block.Text(UUID.randomUUID().toString(), "")
    }

    return blocks.toMutableList().also {
        it[index] = newBlock
    }
}
