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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompactPrayerWidgetProvider : AppWidgetProvider() {

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
            val component = ComponentName(context, CompactPrayerWidgetProvider::class.java)
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
                val views = RemoteViews(context.packageName, R.layout.widget_compact_prayer)
                val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
                val schedulerPrefs = AdhanScheduler.getPrefs(context)

                val city = appPrefs.getString("saved_city_name", null)
                    ?: schedulerPrefs.getString("saved_city_name", "الجزائر العاصمة")
                    ?: "الجزائر العاصمة"
                val appLanguage = appPrefs.getString("app_language", "ar") ?: "ar"
                val numeralSystem = appPrefs.getString("numeral_system", "western") ?: "western"
                val isEn = appLanguage == "en"

                val is24HourFormat = appPrefs.getBoolean("is_24_hour_format", true)

                // Single Source of Truth
                val targetPrayer = NextPrayerTracker.getNextPrayer(context)
                val nextPrayerName = if (isEn) targetPrayer.nameEn else targetPrayer.nameAr
                val nextPrayerTime = AppLocalization.formatTime(targetPrayer.formattedTime, is24HourFormat, appLanguage, numeralSystem)

                // Dates
                val gregorianFormat = if (isEn) {
                    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
                } else {
                    SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
                }
                val gregorianDateStr = gregorianFormat.format(Date())

                val rawHijri = getIcuHijriDate(isEn)
                val hijriDateStr = AppLocalization.getLocalizedHijriDate(rawHijri, appLanguage, short = true)
                val localizedCity = AppLocalization.getLocalizedCityName(city, appLanguage)

                // Theme adaptation
                val isDark = appPrefs.getBoolean("is_dark_mode", false)
                if (isDark) {
                    views.setInt(R.id.widget_compact_root, "setBackgroundResource", R.drawable.bg_compact_widget_dark)
                    views.setTextColor(R.id.tv_compact_location, 0xFF94A3B8.toInt())
                    views.setTextColor(R.id.tv_compact_gregorian, 0xFF94A3B8.toInt())
                    views.setTextColor(R.id.tv_compact_hijri, 0xFF94A3B8.toInt())
                    views.setTextColor(R.id.tv_compact_title, 0xFF94A3B8.toInt())
                    views.setTextColor(R.id.tv_compact_prayer_name, 0xFF4ADE80.toInt())
                    views.setTextColor(R.id.tv_compact_prayer_time, 0xFFE2E8F0.toInt())
                    views.setTextColor(R.id.chronometer_compact, 0xFFF1F5F9.toInt())
                } else {
                    views.setInt(R.id.widget_compact_root, "setBackgroundResource", R.drawable.bg_compact_widget)
                    views.setTextColor(R.id.tv_compact_location, 0xFF5C6B63.toInt())
                    views.setTextColor(R.id.tv_compact_gregorian, 0xFF5C6B63.toInt())
                    views.setTextColor(R.id.tv_compact_hijri, 0xFF5C6B63.toInt())
                    views.setTextColor(R.id.tv_compact_title, 0xFF5C6B63.toInt())
                    views.setTextColor(R.id.tv_compact_prayer_name, 0xFF1E4A38.toInt())
                    views.setTextColor(R.id.tv_compact_prayer_time, 0xFF3A4A42.toInt())
                    views.setTextColor(R.id.chronometer_compact, 0xFF1E4A38.toInt())
                }

                views.setTextViewText(R.id.tv_compact_title, if (isEn) "Next Prayer" else "الصلاة القادمة")
                views.setTextViewText(R.id.tv_compact_prayer_name, nextPrayerName)
                views.setTextViewText(R.id.tv_compact_prayer_time, nextPrayerTime)
                views.setTextViewText(R.id.tv_compact_location, localizedCity)
                views.setTextViewText(R.id.tv_compact_gregorian, gregorianDateStr)
                views.setTextViewText(R.id.tv_compact_hijri, hijriDateStr)

                // Requirement 4: Pure native Chronometer countdown
                val nowMs = System.currentTimeMillis()
                val base = SystemClock.elapsedRealtime() + maxOf(0L, targetPrayer.targetTimeMillis - nowMs)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    views.setChronometerCountDown(R.id.chronometer_compact, true)
                }
                views.setChronometer(R.id.chronometer_compact, base, null, true)
                views.setViewVisibility(R.id.chronometer_compact, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.tv_compact_countdown_label, android.view.View.GONE)

                // Schedule transition alarm for exact target
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
                views.setOnClickPendingIntent(R.id.widget_compact_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                android.util.Log.e("CompactPrayerWidgetProvider", "Failed to update widget", e)
            }
        }

        private fun getIcuHijriDate(isEn: Boolean = false): String {
            return try {
                val icuCal = android.icu.util.IslamicCalendar()
                val day = icuCal.get(android.icu.util.Calendar.DAY_OF_MONTH)
                val month = icuCal.get(android.icu.util.Calendar.MONTH) + 1
                val year = icuCal.get(android.icu.util.Calendar.YEAR)
                AppLocalization.formatHijriDate(day, month, year, if (isEn) "en" else "ar", short = true)
            } catch (_: Exception) {
                ""
            }
        }
    }
}
