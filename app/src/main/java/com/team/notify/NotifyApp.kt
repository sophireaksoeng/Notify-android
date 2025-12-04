package com.team.notify

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.AppDatabaseHolder
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NotifyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        WorkManager.initialize(
            this,
            workManagerConfiguration
        )
        SyncScheduler.schedule(this, "default-space")

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val defaultSpace = SpaceEntity(
                id = "default-space",
                name = "Default Space",
                description = "Auto-created default space",
                createdAt = now,
                updatedAt = now
            )
            appDatabase.spaceDao().insert(defaultSpace)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
