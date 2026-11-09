package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.AdhanScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("BootReceiver", "Received action: $action, rescheduling Adhan alarms...")

        if (action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            try {
                com.example.util.AppUpdateManager.onPackageReplaced(context)
            } catch (_: Exception) {}
        }

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == "android.intent.action.TIME_SET"
        ) {
            val savedTimings = AdhanScheduler.getSavedPrayerTimings(context)
            if (savedTimings != null) {
                AdhanScheduler.scheduleAllPrayers(context, savedTimings)
                Log.d("BootReceiver", "Successfully rescheduled all prayers on boot/time-change.")
            }
            try {
                val request = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.LocationSyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                    .build()
                androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "LocationSyncWork",
                    androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            } catch (_: Exception) {}
            try {
                com.example.util.NextPrayerTracker.syncAll(context)
            } catch (_: Exception) {}
        }
    }
}
