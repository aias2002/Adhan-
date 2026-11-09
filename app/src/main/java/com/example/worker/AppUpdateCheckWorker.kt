package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.util.AppUpdateManager

class AppUpdateCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d("AppUpdateCheckWorker", "Periodic background update check triggered")
        return try {
            val result = AppUpdateManager.checkForUpdate(applicationContext, isManual = false)
            Log.d("AppUpdateCheckWorker", "Check completed with status: ${result.status}")
            Result.success()
        } catch (e: Exception) {
            Log.e("AppUpdateCheckWorker", "Periodic check encountered error: ${e.message}", e)
            Result.retry()
        }
    }
}
