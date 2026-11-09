package com.example

import android.app.Application
import android.util.Log
import com.example.util.AppUpdateManager

class AdhanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("AdhanApplication", "Application onCreate: initializing update cleanup & scheduling")
        try {
            AppUpdateManager.onAppLaunched(this)
        } catch (e: Exception) {
            Log.e("AdhanApplication", "Failed during update check init: ${e.message}")
        }
    }
}
