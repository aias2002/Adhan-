package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.PrayerAutoSilentManager

class PrayerSilentReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_START_SILENT = "com.example.action.START_PRAYER_SILENT"
        const val ACTION_RESTORE_SOUND = "com.example.action.RESTORE_PRAYER_SOUND"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("PrayerSilentReceiver", "Received action: $action")

        when (action) {
            ACTION_START_SILENT -> {
                val prayerKey = intent.getStringExtra("prayer_key") ?: "prayer"
                val prayerName = intent.getStringExtra("prayer_name") ?: "الصلاة"
                val extraDuration = intent.getIntExtra("duration_minutes", -1)
                val duration = if (extraDuration > 0) {
                    extraDuration
                } else {
                    PrayerAutoSilentManager.getDurationMinutes(context, prayerKey)
                }
                PrayerAutoSilentManager.activateSilentMode(context, prayerKey, prayerName, duration)
            }
            ACTION_RESTORE_SOUND -> {
                PrayerAutoSilentManager.restoreNormalMode(context, showToast = true)
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // If phone was rebooted during silent mode and duration passed, restore sound
                val prefs = PrayerAutoSilentManager.getPrefs(context)
                if (prefs.getBoolean(PrayerAutoSilentManager.KEY_IS_ACTIVE, false)) {
                    val restoreEpochMs = prefs.getLong(PrayerAutoSilentManager.KEY_RESTORE_EPOCH_MS, 0L)
                    if (System.currentTimeMillis() >= restoreEpochMs) {
                        PrayerAutoSilentManager.restoreNormalMode(context, showToast = false)
                    }
                }
            }
        }
    }
}
