package com.team.notify

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.team.notify.taskflow.data.AppDatabase
import com.team.notify.taskflow.data.DemoSeeder
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import com.team.notify.taskflow.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NotifyApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var appDatabase: AppDatabase
    @Inject lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()

        runCatching { FirebaseApp.initializeApp(this) }

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            appDatabase.spaceDao().insert(
                SpaceEntity(
                    id = "default-space",
                    name = "Default Space",
                    description = "Auto-created default space",
                    createdAt = now,
                    updatedAt = now
                )
            )

            val members = listOf(
                SpaceMemberEntity(spaceId = "default-space", userId = "u1", role = "OWNER"),
                SpaceMemberEntity(spaceId = "default-space", userId = "u2", role = "EDITOR"),
                SpaceMemberEntity(spaceId = "default-space", userId = "u3", role = "VIEWER")
            )
            appDatabase.spaceMemberDao().upsertAll(members)

            DemoSeeder.seed(appDatabase)

            Log.d("NotifyApp", "Init done")

            // runCatching { syncScheduler.schedulePeriodic(this@NotifyApp) }
            // runCatching { syncScheduler.scheduleNow(this@NotifyApp) }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
