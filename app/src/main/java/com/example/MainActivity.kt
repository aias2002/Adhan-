package com.example

import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind physical volume keys to media stream
        volumeControlStream = android.media.AudioManager.STREAM_MUSIC

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SalatiGlobalError", "Unhandled exception on thread ${thread.name}", throwable)
        }

        try {
            val request = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.LocationSyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                .build()
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "LocationSyncWork",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } catch (_: Exception) {}

        try {
            enableEdgeToEdge()
            setContent {
                MainScreen()
            }
        } catch (e: Exception) {
            Log.e("SalatiInitError", "Failed to initialize app UI", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            com.example.util.AppUpdateManager.performCleanupIfUpToDate(this)
            com.example.util.NextPrayerTracker.syncAll(this)
            com.example.widget.MorningAdhkarWidgetProvider.updateAllWidgets(this)
            com.example.widget.EveningAdhkarWidgetProvider.updateAllWidgets(this)
        } catch (_: Exception) {}
    }
}
