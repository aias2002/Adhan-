package com.example.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.service.AdhanPlaybackService
import com.example.util.AdhanScheduler

class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val prayerKey = intent?.getStringExtra(AdhanScheduler.KEY_PRAYER_KEY) ?: "dhuhr"
        val prayerName = intent?.getStringExtra(AdhanScheduler.KEY_PRAYER_NAME) ?: "الصلاة"
        val isSunrise = intent?.getBooleanExtra(AdhanScheduler.KEY_IS_SUNRISE, false) ?: false

        Log.d("AdhanAlarmReceiver", "Received alarm for prayer: $prayerKey ($prayerName)")

        // Synchronize all prayer countdowns & widgets immediately upon prayer time arrival
        try {
            com.example.util.NextPrayerTracker.syncAll(context)
        } catch (e: Exception) {
            Log.e("AdhanAlarmReceiver", "Failed to sync interfaces on alarm: ${e.message}")
        }

        val prefs = AdhanScheduler.getPrefs(context)
        val isEnabled = if (isSunrise) {
            prefs.getBoolean("use_rooster_for_sunrise", true)
        } else {
            prefs.getBoolean("adhan_$prayerKey", true)
        }

        if (isEnabled) {
            val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = AdhanPlaybackService.ACTION_START
                putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayerName)
                putExtra(AdhanPlaybackService.EXTRA_PRAYER_KEY, prayerKey)
                putExtra(AdhanPlaybackService.EXTRA_IS_SUNRISE, isSunrise)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("AdhanAlarmReceiver", "Failed to start AdhanPlaybackService: ${e.message}")
            }
            try {
                if (!isSunrise) {
                    com.example.util.PrayerAutoSilentManager.onPrayerTriggered(context, prayerKey, prayerName, isAdhanAlreadyPlayed = false)
                }
            } catch (_: Exception) {}
        } else {
            Log.d("AdhanAlarmReceiver", "Adhan for $prayerKey is disabled by user.")
            try {
                if (!isSunrise) {
                    com.example.util.PrayerAutoSilentManager.onPrayerMutedArrival(context, prayerKey, prayerName)
                }
            } catch (_: Exception) {}
        }

        // Reschedule next occurrence for this prayer
        val savedTimings = AdhanScheduler.getSavedPrayerTimings(context)
        if (savedTimings != null) {
            val timeStr = when (prayerKey) {
                "fajr" -> savedTimings.fajr
                "sunrise" -> savedTimings.sunrise
                "dhuhr" -> savedTimings.dhuhr
                "asr" -> savedTimings.asr
                "maghrib" -> savedTimings.maghrib
                "isha" -> savedTimings.isha
                else -> ""
            }
            val reqCode = when (prayerKey) {
                "fajr" -> AdhanScheduler.CODE_FAJR
                "sunrise" -> AdhanScheduler.CODE_SUNRISE
                "dhuhr" -> AdhanScheduler.CODE_DHUHR
                "asr" -> AdhanScheduler.CODE_ASR
                "maghrib" -> AdhanScheduler.CODE_MAGHRIB
                "isha" -> AdhanScheduler.CODE_ISHA
                else -> 100
            }
            if (timeStr.isNotEmpty()) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                if (alarmManager != null) {
                    AdhanScheduler.scheduleSinglePrayer(
                        context,
                        alarmManager,
                        prayerKey,
                        prayerName,
                        timeStr.take(5),
                        reqCode,
                        isSunrise
                    )
                }
            }
        }
    }
}
