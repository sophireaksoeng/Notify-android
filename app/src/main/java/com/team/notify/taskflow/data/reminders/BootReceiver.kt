package com.team.notify.taskflow.data.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.team.notify.taskflow.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Re-scheduling reminders…")

            CoroutineScope(Dispatchers.IO).launch {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notify.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabase.seedCallback())
                    .build()

                ReminderScheduler.rescheduleAll(context, db.taskDao())
            }
        }
    }
}
