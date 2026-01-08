package com.team.notify.taskflow.domain.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LabelsCodec {
    private val gson = Gson()
    private val type = object : TypeToken<List<String>>() {}.type

    fun encode(labels: List<String>): String = gson.toJson(labels)

    fun decode(labelsJson: String?): List<String> {
        if (labelsJson.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<String>>(labelsJson, type) }
            .getOrElse { emptyList() }
    }
}
