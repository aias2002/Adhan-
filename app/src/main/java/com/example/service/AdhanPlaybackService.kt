package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PRESET_ADHAN_SOUNDS
import com.example.util.AdhanAudioPlayer
import com.example.util.AdhanScheduler

class AdhanPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "adhan_playback_channel_v2"
        const val NOTIFICATION_ID = 9991
        const val ACTION_START = "com.example.action.START_ADHAN"
        const val ACTION_STOP = "com.example.action.STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_KEY = "extra_prayer_key"
        const val EXTRA_IS_SUNRISE = "extra_is_sunrise"
        const val ACTION_ADHAN_STOPPED = "com.example.action.ADHAN_STOPPED"
    }

    private var audioPlayer: AdhanAudioPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var flipDetector: com.example.util.FlipDetector? = null

    override fun onCreate() {
        super.onCreate()
        audioPlayer = AdhanAudioPlayer(this)
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Salati:AdhanPlaybackWakeLock"
        )?.apply {
            setReferenceCounted(false)
            acquire(6 * 60 * 1000L) // 6 minutes max safety
        }
        createNotificationChannel()
    }

    private var currentPrayerName: String = "الصلاة"
    private var currentPrayerKey: String = "dhuhr"
    private var currentIsSunrise: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        if (action == ACTION_STOP) {
            stopAdhanAndSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val prayerKey = intent?.getStringExtra(EXTRA_PRAYER_KEY) ?: "dhuhr"
        val isSunrise = intent?.getBooleanExtra(EXTRA_IS_SUNRISE, false) ?: false

        currentPrayerName = prayerName
        currentPrayerKey = prayerKey
        currentIsSunrise = isSunrise

        val notification = buildForegroundNotification(prayerName, prayerKey, isSunrise)
        startForeground(NOTIFICATION_ID, notification)

        playAdhan(prayerName, prayerKey, isSunrise)

        return START_NOT_STICKY
    }

    private fun playAdhan(prayerName: String, prayerKey: String, isSunrise: Boolean) {
        val prefs = AdhanScheduler.getPrefs(this)
        val isEnabled = if (isSunrise) {
            prefs.getBoolean("use_rooster_for_sunrise", true)
        } else {
            prefs.getBoolean("adhan_${prayerKey.lowercase()}", true)
        }

        if (!isEnabled) {
            stopAdhanAndSelf()
            return
        }

        val overrideSilent = prefs.getBoolean("override_silent_mode", true)
        val soundTarget = com.example.util.AdhanAudioResolver.resolveFromPrefs(
            context = this,
            prayerKey = prayerKey,
            prayerName = prayerName,
            isSunrise = isSunrise
        )

        val audioStreamType = prefs.getString("adhan_audio_stream_type", "media") ?: "media"
        audioPlayer?.playSound(soundTarget, overrideSilentMode = overrideSilent, streamType = audioStreamType) {
            stopAdhanAndSelf()
        }

        val useFlipToMute = prefs.getBoolean("use_flip_to_mute", true)
        if (useFlipToMute) {
            flipDetector = com.example.util.FlipDetector(this) {
                stopAdhanAndSelf()
            }
            flipDetector?.startListening()
        }
    }

    private fun buildForegroundNotification(prayerName: String, prayerKey: String, isSunrise: Boolean): Notification {
        val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, pFlags)

        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, pFlags)

        val fullScreenIntent = Intent(this, com.example.ui.adhan.AdhanAlarmActivity::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_KEY, prayerKey)
            putExtra(EXTRA_IS_SUNRISE, isSunrise)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 2, fullScreenIntent, pFlags)

        val title = if (isSunrise) {
            "حان وقت الشروق"
        } else {
            "حان الآن موعد أذان $prayerName"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)

        if (isSunrise) {
            builder.setContentText("حان وقت الشروق")
        } else {
            builder.setContentText("Adhan+ - نداء الصلاة")
        }

        val stopLabel = if (isSunrise) "إيقاف التنبيه ⏹" else "إيقاف الأذان ⏹"
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, stopLabel, stopPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "أذان وتنبيهات الصلوات"
            val descriptionText = "تنبيهات صوت الأذان في موعد كل صلاة"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                setBypassDnd(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun stopAdhanAndSelf() {
        try {
            val stoppedIntent = Intent(ACTION_ADHAN_STOPPED).apply {
                setPackage(packageName)
            }
            sendBroadcast(stoppedIntent)
        } catch (_: Exception) {}

        if (!currentIsSunrise && currentPrayerKey.isNotEmpty()) {
            try {
                com.example.util.PrayerAutoSilentManager.onPrayerTriggered(
                    this,
                    currentPrayerKey,
                    currentPrayerName,
                    isAdhanAlreadyPlayed = true
                )
            } catch (_: Exception) {}
        }

        audioPlayer?.stopSound()
        try {
            flipDetector?.stopListening()
            flipDetector = null
        } catch (_: Exception) {}
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        stopAdhanAndSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
