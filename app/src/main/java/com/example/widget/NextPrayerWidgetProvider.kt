package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.ui.util.AppLocalization
import com.example.util.AdhanScheduler
import com.example.util.NextPrayerTracker

class NextPrayerWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, NextPrayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            for (id in appWidgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_next_prayer)
                val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
                val schedulerPrefs = AdhanScheduler.getPrefs(context)

                val city = appPrefs.getString("saved_city_name", null)
                    ?: schedulerPrefs.getString("saved_city_name", "الجزائر العاصمة")
                    ?: "الجزائر العاصمة"
                val appLanguage = appPrefs.getString("app_language", "ar") ?: "ar"
                val isAr = appLanguage != "en"

                // Single Source of Truth
                val targetPrayer = NextPrayerTracker.getNextPrayer(context)
                val localizedCity = AppLocalization.getLocalizedCityName(city, appLanguage)
                val nextPrayerName = if (isAr) targetPrayer.nameAr else targetPrayer.nameEn

                // Theme adaptation
                val isDark = appPrefs.getBoolean("is_dark_mode", false)
                views.setViewVisibility(R.id.tv_next_prayer_location, android.view.View.GONE)
                if (isDark) {
                    views.setInt(R.id.widget_next_prayer_root, "setBackgroundResource", R.drawable.bg_compact_widget_dark)
                    views.setTextColor(R.id.tv_next_prayer_name, 0xFF4ADE80.toInt())
                    views.setTextColor(R.id.chronometer_next_prayer, 0xFFF1F5F9.toInt())
                } else {
                    views.setInt(R.id.widget_next_prayer_root, "setBackgroundResource", R.drawable.bg_compact_widget)
                    views.setTextColor(R.id.tv_next_prayer_name, 0xFF1E4A38.toInt())
                    views.setTextColor(R.id.chronometer_next_prayer, 0xFF1E4A38.toInt())
                }

                views.setTextViewText(R.id.tv_next_prayer_name, nextPrayerName)

                // Requirement 4: Pure native Chronometer countdown: SystemClock.elapsedRealtime() + (target - now)
                val nowMs = System.currentTimeMillis()
                val base = SystemClock.elapsedRealtime() + maxOf(0L, targetPrayer.targetTimeMillis - nowMs)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    views.setChronometerCountDown(R.id.chronometer_next_prayer, true)
                }
                views.setChronometer(R.id.chronometer_next_prayer, base, null, true)
                views.setViewVisibility(R.id.chronometer_next_prayer, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.tv_next_prayer_countdown_label, android.view.View.GONE)

                // Ensure transition alarm is scheduled for exact target
                NextPrayerTracker.scheduleTransitionAlarm(context, targetPrayer.targetTimeMillis)

                // Click intent to open app
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, pFlags)
                views.setOnClickPendingIntent(R.id.widget_next_prayer_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                android.util.Log.e("NextPrayerWidgetProvider", "Failed to update widget", e)
            }
        }
    }
}
