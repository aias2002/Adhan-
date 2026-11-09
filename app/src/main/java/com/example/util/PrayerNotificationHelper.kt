package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.util.AppLocalization

object PrayerNotificationHelper {

    private const val CHANNEL_ID = "next_prayer_lockscreen_channel"
    private const val NOTIFICATION_ID = 2001

    /**
     * Requirement 3: Unified single-source notification.
     * Uses Android's native Chronometer countdown rendered smoothly by the OS
     * without per-second or per-minute manual timer updates.
     */
    fun updateNotification(context: Context) {
        val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
        val isEnabled = appPrefs.getBoolean("show_lockscreen_prayer_notification", true)

        if (!isEnabled) {
            cancelNotification(context)
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        createNotificationChannel(context, notificationManager)

        val appLanguage = appPrefs.getString("app_language", "ar") ?: "ar"
        val isAr = appLanguage != "en"
        val numeralSystem = appPrefs.getString("numeral_system", "western") ?: "western"

        val schedulerPrefs = AdhanScheduler.getPrefs(context)
        val city = appPrefs.getString("saved_city_name", null)
            ?: schedulerPrefs.getString("saved_city_name", "الجزائر العاصمة")
            ?: "الجزائر العاصمة"
        val localizedCity = AppLocalization.getLocalizedCityName(city, appLanguage)

        // Read directly from the Single Source of Truth
        val target = NextPrayerTracker.getNextPrayer(context)
        val nextName = if (isAr) target.nameAr else target.nameEn
        val prevName = if (isAr) target.previousNameAr else target.previousNameEn
        val formattedNextTime = AppLocalization.formatDigits(target.formattedTime, numeralSystem)

        // Line 1: الأذان القادم : الفجر 05:33
        val nextLabel = if (isAr) "الأذان القادم : " else "Next Adhan : "
        val nextVal = "$nextName $formattedNextTime"

        // Line 2: الوقت المتبقي : 08:01:31
        val remainingLabel = if (isAr) "الوقت المتبقي : " else "Remaining : "

        // Line 3: مضى على اذان العشاء : 00:00:00
        val elapsedLabel = if (isAr) "مضى على اذان $prevName : " else "Elapsed since $prevName : "

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(context, 0, openIntent, pFlags)

        val openActionTitle = if (isAr) "فتح التطبيق" else "Open App"
        val openAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            openActionTitle,
            contentPendingIntent
        ).build()

        // Custom RemoteViews with fixed, responsive 3-line layout
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_prayer).apply {
            setTextViewText(R.id.tv_notification_next_label, nextLabel)
            setTextViewText(R.id.tv_notification_next_val, nextVal)
            setTextViewText(R.id.tv_notification_remaining_label, remainingLabel)
            setTextViewText(R.id.tv_notification_elapsed_label, elapsedLabel)

            val nowMs = System.currentTimeMillis()

            // Line 2: Countdown to next prayer
            val baseNext = SystemClock.elapsedRealtime() + maxOf(0L, target.targetTimeMillis - nowMs)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setChronometerCountDown(R.id.chrono_notification_remaining, true)
            }
            setChronometer(R.id.chrono_notification_remaining, baseNext, null, true)

            // Line 3: Elapsed time since previous prayer
            val elapsedSincePrevMs = maxOf(0L, nowMs - target.previousTimeMillis)
            val basePrev = SystemClock.elapsedRealtime() - elapsedSincePrevMs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setChronometerCountDown(R.id.chrono_notification_elapsed, false)
            }
            setChronometer(R.id.chrono_notification_elapsed, basePrev, null, true)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (isAr) "$nextLabel$nextVal" else "$nextLabel$nextVal")
            .setContentText(if (isAr) "تطبيق مواقيت الصلاة والأذان" else "Prayer times & Adhan")
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible on Lock Screen
            .setOngoing(true) // Pinned ongoing status notification
            .setOnlyAlertOnce(true) // Silent updates (no sound/vibrate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .addAction(openAction)

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: Exception) {}
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context, manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
            val isAr = (appPrefs.getString("app_language", "ar") ?: "ar") != "en"
            val channelName = if (isAr) "إشعار الصلاة القادمة (شاشة القفل)" else "Next Prayer Lock Screen Notification"
            val channelDesc = if (isAr) "عرض الصلاة القادمة والعد التنازلي في لوحة الإشعارات وشاشة القفل" else "Displays next prayer and countdown on lock screen"

            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = channelDesc
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }
}
