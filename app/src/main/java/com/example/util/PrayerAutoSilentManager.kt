package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.receiver.PrayerSilentReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrayerAutoSilentManager {
    private const val TAG = "PrayerAutoSilentManager"
    private const val PREFS_NAME = "prayer_auto_silent_prefs"

    const val CHANNEL_ID = "prayer_silent_notification_channel"
    const val NOTIFICATION_ID = 8812

    const val REQ_CODE_START_SILENT = 7711
    const val REQ_CODE_RESTORE_SOUND = 7712

    // Preference Keys
    const val KEY_ENABLED = "auto_silent_enabled"
    const val KEY_MODE_TYPE = "silent_mode_type" // "silent" or "vibrate"
    const val KEY_DELAY_MINUTES = "delay_minutes_after_adhan" // Global fallback: 0 (فوراً), 5, 10, 15
    const val KEY_DURATION_MINUTES = "duration_minutes" // Global fallback: 15, 20, 30, 45
    const val KEY_TRANSITION_ALERT = "silent_transition_alert_enabled" // تنبيه خفيف عند الانتقال والخروج
    const val KEY_SILENT_FAJR = "silent_fajr"
    const val KEY_SILENT_DHUHR = "silent_dhuhr"
    const val KEY_SILENT_ASR = "silent_asr"
    const val KEY_SILENT_MAGHRIB = "silent_maghrib"
    const val KEY_SILENT_ISHA = "silent_isha"
    const val KEY_SILENT_JUMUAH = "silent_jumuah"

    // Per-prayer delay keys (minutes after adhan to start silent mode)
    const val KEY_DELAY_FAJR = "delay_fajr"
    const val KEY_DELAY_DHUHR = "delay_dhuhr"
    const val KEY_DELAY_ASR = "delay_asr"
    const val KEY_DELAY_MAGHRIB = "delay_maghrib"
    const val KEY_DELAY_ISHA = "delay_isha"
    const val KEY_DELAY_JUMUAH = "delay_jumuah"

    // Per-prayer duration keys (minutes of silent mode)
    const val KEY_DURATION_FAJR = "duration_fajr"
    const val KEY_DURATION_DHUHR = "duration_dhuhr"
    const val KEY_DURATION_ASR = "duration_asr"
    const val KEY_DURATION_MAGHRIB = "duration_maghrib"
    const val KEY_DURATION_ISHA = "duration_isha"
    const val KEY_DURATION_JUMUAH = "duration_jumuah"

    // Active State Tracking Keys
    const val KEY_IS_ACTIVE = "is_silent_active"
    const val KEY_PREVIOUS_RINGER_MODE = "previous_ringer_mode"
    const val KEY_RESTORE_EPOCH_MS = "restore_epoch_millis"
    const val KEY_ACTIVE_PRAYER_NAME = "active_prayer_name"

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAutoSilentEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, false)
    }

    fun hasNotificationPolicyAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            return nm?.isNotificationPolicyAccessGranted == true
        }
        return true
    }

    fun openNotificationPolicySettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open notification policy settings: ${e.message}")
        }
    }

    fun isPrayerEnabled(context: Context, prayerKey: String): Boolean {
        val prefs = getPrefs(context)
        val isFriday = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        if (isFriday && (prayerKey.lowercase() == "dhuhr" || prayerKey.lowercase() == "jumuah")) {
            return prefs.getBoolean(KEY_SILENT_JUMUAH, true)
        }
        return when (prayerKey.lowercase()) {
            "fajr" -> prefs.getBoolean(KEY_SILENT_FAJR, true)
            "dhuhr" -> prefs.getBoolean(KEY_SILENT_DHUHR, true)
            "asr" -> prefs.getBoolean(KEY_SILENT_ASR, true)
            "maghrib" -> prefs.getBoolean(KEY_SILENT_MAGHRIB, true)
            "isha" -> prefs.getBoolean(KEY_SILENT_ISHA, true)
            "jumuah" -> prefs.getBoolean(KEY_SILENT_JUMUAH, true)
            else -> true
        }
    }

    fun getDelayMinutes(context: Context, prayerKey: String): Int {
        val prefs = getPrefs(context)
        val isFriday = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        val effectiveKey = if (isFriday && (prayerKey.lowercase() == "dhuhr" || prayerKey.lowercase() == "jumuah")) "jumuah" else prayerKey.lowercase()
        val defaultDelay = prefs.getInt(KEY_DELAY_MINUTES, 0)
        return when (effectiveKey) {
            "fajr" -> prefs.getInt(KEY_DELAY_FAJR, defaultDelay)
            "dhuhr" -> prefs.getInt(KEY_DELAY_DHUHR, defaultDelay)
            "asr" -> prefs.getInt(KEY_DELAY_ASR, defaultDelay)
            "maghrib" -> prefs.getInt(KEY_DELAY_MAGHRIB, defaultDelay)
            "isha" -> prefs.getInt(KEY_DELAY_ISHA, defaultDelay)
            "jumuah" -> prefs.getInt(KEY_DELAY_JUMUAH, 0)
            else -> defaultDelay
        }
    }

    fun getDurationMinutes(context: Context, prayerKey: String): Int {
        val prefs = getPrefs(context)
        val isFriday = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        val effectiveKey = if (isFriday && (prayerKey.lowercase() == "dhuhr" || prayerKey.lowercase() == "jumuah")) "jumuah" else prayerKey.lowercase()
        val defaultDuration = prefs.getInt(KEY_DURATION_MINUTES, 20)
        return when (effectiveKey) {
            "fajr" -> prefs.getInt(KEY_DURATION_FAJR, 25)
            "dhuhr" -> prefs.getInt(KEY_DURATION_DHUHR, 20)
            "asr" -> prefs.getInt(KEY_DURATION_ASR, 20)
            "maghrib" -> prefs.getInt(KEY_DURATION_MAGHRIB, 20)
            "isha" -> prefs.getInt(KEY_DURATION_ISHA, 25)
            "jumuah" -> prefs.getInt(KEY_DURATION_JUMUAH, 45)
            else -> defaultDuration
        }
    }

    /**
     * Called when a prayer time arrives (or after adhan finishes).
     */
    fun onPrayerTriggered(context: Context, prayerKey: String, prayerName: String, isAdhanAlreadyPlayed: Boolean = false) {
        if (!isAutoSilentEnabled(context)) return
        if (prayerKey.lowercase() == "sunrise") return // No silent mode for sunrise
        if (!isPrayerEnabled(context, prayerKey)) {
            Log.d(TAG, "Auto-silent disabled for $prayerKey")
            return
        }

        val isFriday = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        val effectivePrayerName = if (isFriday && prayerKey.lowercase() == "dhuhr") "الجمعة" else prayerName

        val delayMinutes = getDelayMinutes(context, prayerKey)
        val durationMinutes = getDurationMinutes(context, prayerKey)

        if (delayMinutes <= 0 || isAdhanAlreadyPlayed) {
            activateSilentMode(context, prayerKey, effectivePrayerName, durationMinutes)
        } else {
            // Schedule delayed silent activation (custom minutes after adhan)
            scheduleDelayedActivation(context, prayerKey, effectivePrayerName, delayMinutes, durationMinutes)
        }
    }

    fun onPrayerMutedArrival(context: Context, prayerKey: String, prayerName: String) {
        if (!isAutoSilentEnabled(context)) return
        if (prayerKey.lowercase() == "sunrise") return
        if (!isPrayerEnabled(context, prayerKey)) return

        val isFriday = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
        val effectivePrayerName = if (isFriday && prayerKey.lowercase() == "dhuhr") "الجمعة" else prayerName

        val delayMinutes = getDelayMinutes(context, prayerKey)
        val durationMinutes = getDurationMinutes(context, prayerKey)
        if (delayMinutes <= 0) {
            activateSilentMode(context, prayerKey, effectivePrayerName, durationMinutes)
        } else {
            scheduleDelayedActivation(context, prayerKey, effectivePrayerName, delayMinutes, durationMinutes)
        }
    }

    private fun scheduleDelayedActivation(context: Context, prayerKey: String, prayerName: String, delayMinutes: Int, durationMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMs = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

        val intent = Intent(context, PrayerSilentReceiver::class.java).apply {
            action = PrayerSilentReceiver.ACTION_START_SILENT
            putExtra("prayer_key", prayerKey)
            putExtra("prayer_name", prayerName)
            putExtra("duration_minutes", durationMinutes)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REQ_CODE_START_SILENT, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
            Log.d(TAG, "Scheduled delayed silent mode in $delayMinutes min ($durationMinutes min duration) at ${Date(triggerAtMs)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule delayed silent mode: ${e.message}")
        }
    }

    fun activateSilentMode(context: Context, prayerKey: String, prayerName: String, durationMinutes: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val prefs = getPrefs(context)

            // Save original state before changing
            if (!prefs.getBoolean(KEY_IS_ACTIVE, false)) {
                val currentRinger = audioManager.ringerMode
                prefs.edit().putInt(KEY_PREVIOUS_RINGER_MODE, currentRinger).apply()
            }

            val modeType = prefs.getString(KEY_MODE_TYPE, "silent") ?: "silent"
            val targetMode = if (modeType == "vibrate") {
                AudioManager.RINGER_MODE_VIBRATE
            } else {
                AudioManager.RINGER_MODE_SILENT
            }

            // Apply ringer mode safely
            if (hasNotificationPolicyAccess(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                audioManager.ringerMode = targetMode
            } else {
                // Best effort if permission not granted
                try {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                } catch (_: Exception) {}
            }

            val restoreEpochMs = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            prefs.edit()
                .putBoolean(KEY_IS_ACTIVE, true)
                .putLong(KEY_RESTORE_EPOCH_MS, restoreEpochMs)
                .putString(KEY_ACTIVE_PRAYER_NAME, prayerName)
                .apply()

            // Emit polite gentle transition alert (gentle Islamic/tone + soft haptic)
            playTransitionAlert(context, isEnteringSilent = true)

            // Post persistent control notification
            showSilentNotification(context, prayerName, restoreEpochMs, modeType)

            // Schedule automatic restoration
            scheduleRestoreAlarm(context, restoreEpochMs)

            Log.d(TAG, "Activated silent mode for $prayerName until ${Date(restoreEpochMs)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error in activateSilentMode: ${e.message}", e)
        }
    }

    fun restoreNormalMode(context: Context, showToast: Boolean = true) {
        try {
            val prefs = getPrefs(context)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val savedRinger = prefs.getInt(KEY_PREVIOUS_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL)

            if (audioManager != null) {
                if (hasNotificationPolicyAccess(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    audioManager.ringerMode = savedRinger
                } else {
                    try {
                        audioManager.ringerMode = savedRinger
                    } catch (_: Exception) {}
                }
            }

            cancelSilentNotification(context)
            cancelRestoreAlarm(context)

            prefs.edit()
                .putBoolean(KEY_IS_ACTIVE, false)
                .putLong(KEY_RESTORE_EPOCH_MS, 0L)
                .putString(KEY_ACTIVE_PRAYER_NAME, "")
                .apply()

            // Emit polite gentle transition alert on sound restoration
            playTransitionAlert(context, isEnteringSilent = false)

            if (showToast) {
                val modeDesc = when (savedRinger) {
                    AudioManager.RINGER_MODE_VIBRATE -> "الاهتزاز"
                    AudioManager.RINGER_MODE_SILENT -> "الصامت"
                    else -> "الوضع العام"
                }
                Toast.makeText(context, "تمت استعادة $modeDesc للهاتف تلقائياً 🔔", Toast.LENGTH_SHORT).show()
            }
            Log.d(TAG, "Restored normal phone mode: $savedRinger")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring normal mode: ${e.message}", e)
        }
    }

    /**
     * Emits a gentle, polite Islamic alert tone and subtle haptic vibration
     * when switching to silent mode or restoring normal sound mode.
     */
    fun playTransitionAlert(context: Context, isEnteringSilent: Boolean) {
        try {
            val prefs = getPrefs(context)
            val isAlertEnabled = prefs.getBoolean(KEY_TRANSITION_ALERT, true)
            if (!isAlertEnabled) return

            // 1. Gentle tactile haptic pulse
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (isEnteringSilent) {
                            // Gentle double pulse indicating muting
                            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 70, 80), -1))
                        } else {
                            // Gentle pleasant single pulse indicating sound restored
                            v.vibrate(VibrationEffect.createOneShot(130, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        if (isEnteringSilent) {
                            v.vibrate(longArrayOf(0, 80, 70, 80), -1)
                        } else {
                            v.vibrate(130)
                        }
                    }
                }
            }

            // 2. Gentle Polite Audio Tone (Soft chime)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Use STREAM_NOTIFICATION with gentle volume
                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
                    if (isEnteringSilent) {
                        // Soft courtesy prompt tone
                        toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 160)
                    } else {
                        // Soft pleasant affirmative chime
                        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 220)
                    }
                    delay(300)
                    toneGen.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in playTransitionAlert: ${e.message}")
        }
    }

    private fun scheduleRestoreAlarm(context: Context, restoreEpochMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerSilentReceiver::class.java).apply {
            action = PrayerSilentReceiver.ACTION_RESTORE_SOUND
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REQ_CODE_RESTORE_SOUND, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restoreEpochMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, restoreEpochMs, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule restore alarm: ${e.message}")
        }
    }

    private fun cancelRestoreAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, PrayerSilentReceiver::class.java).apply {
                action = PrayerSilentReceiver.ACTION_RESTORE_SOUND
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }
            val pendingIntent = PendingIntent.getBroadcast(context, REQ_CODE_RESTORE_SOUND, intent, flags)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (_: Exception) {}
    }

    private fun showSilentNotification(context: Context, prayerName: String, restoreEpochMs: Long, modeType: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        createNotificationChannel(context, nm)

        val restoreIntent = Intent(context, PrayerSilentReceiver::class.java).apply {
            action = PrayerSilentReceiver.ACTION_RESTORE_SOUND
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val restorePendingIntent = PendingIntent.getBroadcast(context, 1, restoreIntent, flags)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags)

        val timeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date(restoreEpochMs))
        val modeAr = if (modeType == "vibrate") "وضع الاهتزاز" else "الوضع الصامت"

        val title = "🕌 وضع الصلاة مفعّل ($prayerName)"
        val content = "الهاتف في $modeAr حتى الساعة $timeStr"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .addAction(android.R.drawable.ic_lock_silent_mode_off, "استعادة الصوت الآن 🔔", restorePendingIntent)

        try {
            nm.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show silent notification: ${e.message}")
        }
    }

    private fun cancelSilentNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context, nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "وضع الصلاة الصامت"
            val desc = "إشعارات وضع الصلاة والتحكم في استعادة الصوت"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }
}
