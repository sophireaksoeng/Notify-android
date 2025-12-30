package com.team.notify.taskflow.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.team.notify.taskflow.data.AppDatabase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return

        val appContext = context.applicationContext

        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BootReceiverEntryPoint::class.java
        )
        val db = entryPoint.db()

        CoroutineScope(Dispatchers.IO).launch {
            ReminderScheduler.rescheduleAll(appContext, db.taskDao())
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun db(): AppDatabase
}
