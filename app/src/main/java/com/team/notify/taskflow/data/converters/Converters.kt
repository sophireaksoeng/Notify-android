package com.team.notify.taskflow.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.team.notify.taskflow.model.TaskStatus
import com.team.notify.taskflow.data.entities.UserPreferences

class Converters {

    private val gson = Gson()
    
    @TypeConverter
    fun taskStatusToString(status: TaskStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun stringToTaskStatus(value: String?): TaskStatus? {
        if (value.isNullOrBlank()) return null
        return try {
            TaskStatus.valueOf(value)
        } catch (_: Exception) {
            TaskStatus.TODO
        }
    }

    @TypeConverter
    fun stringListToJson(list: List<String>?): String? {
        return if (list == null) null else gson.toJson(list)
    }

    @TypeConverter
    fun jsonToStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun userPreferencesToJson(preferences: UserPreferences?): String? {
        return if (preferences == null) null else gson.toJson(preferences)
    }

    @TypeConverter
    fun jsonToUserPreferences(json: String?): UserPreferences? {
        if (json.isNullOrBlank()) return null
        return try {
            gson.fromJson(json, UserPreferences::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
