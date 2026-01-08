package com.team.notify.taskflow.domain.util

import com.team.notify.taskflow.domain.model.Block
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BlockSerializer {

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(blocks: List<Block>): String =
        json.encodeToString(blocks)

    fun decode(raw: String): List<Block> =
        runCatching {
            json.decodeFromString<List<Block>>(raw)
        }.getOrElse { emptyList() }
}