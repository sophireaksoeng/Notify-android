package com.team.notify.taskflow.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.team.notify.taskflow.data.converters.Converters
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.entities.OperationEntity

@Database(
    entities = [SpaceEntity::class, PageEntity::class, TaskEntity::class, OperationEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun pageDao(): PageDao
    abstract fun taskDao(): TaskDao
    abstract fun opQueueDao(): OpQueueDao
}
