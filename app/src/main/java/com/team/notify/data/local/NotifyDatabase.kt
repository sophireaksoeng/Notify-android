package com.team.notify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.team.notify.data.local.dao.PageDao
import com.team.notify.data.local.dao.SpaceDao
import com.team.notify.data.local.dao.TaskDao
import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity

@Database(
    entities = [
        SpaceEntity::class,
        PageEntity::class,
        TaskEntity::class,
    ],
    version = 9,
)
abstract class NotifyDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun pageDao(): PageDao
    abstract fun taskDao(): TaskDao
}
