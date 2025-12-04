package com.team.notify.taskflow.data

object AppDatabaseHolder {
    @Volatile
    var database: AppDatabase? = null
}
