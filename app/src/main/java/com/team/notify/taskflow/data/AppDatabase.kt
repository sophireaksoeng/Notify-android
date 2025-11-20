package com.team.notify.taskflow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.team.notify.taskflow.data.converters.Converters
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity

@Database(entities = [SpaceEntity::class, PageEntity::class, TaskEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun pageDao(): PageDao
    abstract fun taskDao(): TaskDao
}
