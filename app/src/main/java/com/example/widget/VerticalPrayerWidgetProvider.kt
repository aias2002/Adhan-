package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerTimings
import com.example.ui.util.AppLocalization
import com.example.util.AdhanScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VerticalPrayerWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            com.example.util.NextPrayerTracker.ACTION_PRAYER_TRANSITION,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, VerticalPrayerWidgetProvider::class.java)
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
            val views = RemoteViews(context.packageName, R.layout.widget_vertical_prayer_clock)
            try {
                val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
                val appLanguage = appPrefs.getString("app_language", "ar") ?: "ar"
                val numeralSystem = appPrefs.getString("numeral_system", "western") ?: "western"
                val is24HourFormat = appPrefs.getBoolean("is_24_hour_format", true)
                val isDark = appPrefs.getBoolean("is_dark_mode", false)
                val isEn = appLanguage == "en"

                val timings = AdhanScheduler.getEffectivePrayerTimings(context)

                // Prayer names
                val fajrName = if (isEn) "Fajr" else "الفجر"
                val sunriseName = if (isEn) "Sunrise" else "الشروق"
                val dhuhrName = if (isEn) "Dhuhr" else "الظهر"
                val asrName = if (isEn) "Asr" else "العصر"
                val maghribName = if (isEn) "Maghrib" else "المغرب"
                val ishaName = if (isEn) "Isha" else "العشاء"

                // Format times according to 12h/24h setting and numeral system
                val fajrTime = AppLocalization.formatTime(timings.fajr, is24HourFormat, appLanguage, numeralSystem)
                val sunriseTime = AppLocalization.formatTime(timings.sunrise, is24HourFormat, appLanguage, numeralSystem)
                val dhuhrTime = AppLocalization.formatTime(timings.dhuhr, is24HourFormat, appLanguage, numeralSystem)
                val asrTime = AppLocalization.formatTime(timings.asr, is24HourFormat, appLanguage, numeralSystem)
                val maghribTime = AppLocalization.formatTime(timings.maghrib, is24HourFormat, appLanguage, numeralSystem)
                val ishaTime = AppLocalization.formatTime(timings.isha, is24HourFormat, appLanguage, numeralSystem)

                views.setTextViewText(R.id.tv_vertical_fajr_name, fajrName)
                views.setTextViewText(R.id.tv_vertical_fajr_time, fajrTime)

                views.setTextViewText(R.id.tv_vertical_sunrise_name, sunriseName)
                views.setTextViewText(R.id.tv_vertical_sunrise_time, sunriseTime)

                views.setTextViewText(R.id.tv_vertical_dhuhr_name, dhuhrName)
                views.setTextViewText(R.id.tv_vertical_dhuhr_time, dhuhrTime)

                views.setTextViewText(R.id.tv_vertical_asr_name, asrName)
                views.setTextViewText(R.id.tv_vertical_asr_time, asrTime)

                views.setTextViewText(R.id.tv_vertical_maghrib_name, maghribName)
                views.setTextViewText(R.id.tv_vertical_maghrib_time, maghribTime)

                views.setTextViewText(R.id.tv_vertical_isha_name, ishaName)
                views.setTextViewText(R.id.tv_vertical_isha_time, ishaTime)

                val nextPrayer = com.example.util.NextPrayerTracker.getNextPrayer(context)
                val nextIndex = when (nextPrayer.prayerKey) {
                    "fajr" -> 0
                    "sunrise" -> 1
                    "dhuhr" -> 2
                    "asr" -> 3
                    "maghrib" -> 4
                    "isha" -> 5
                    else -> findNextPrayerIndex(timings)
                }

                // Setup countdown chronometers for rows
                val rowChronos = listOf(
                    R.id.chronometer_vertical_fajr,
                    R.id.chronometer_vertical_sunrise,
                    R.id.chronometer_vertical_dhuhr,
                    R.id.chronometer_vertical_asr,
                    R.id.chronometer_vertical_maghrib,
                    R.id.chronometer_vertical_isha
                )

                val nowMs = System.currentTimeMillis()
                val base = android.os.SystemClock.elapsedRealtime() + maxOf(0L, nextPrayer.targetTimeMillis - nowMs)

                for (i in rowChronos.indices) {
                    val chronoId = rowChronos[i]
                    if (i == nextIndex) {
                        views.setViewVisibility(chronoId, android.view.View.VISIBLE)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            views.setChronometerCountDown(chronoId, true)
                        }
                        views.setChronometer(chronoId, base, null, true)
                        val highlightColor = if (isDark) 0xFF4ADE80.toInt() else 0xFF059669.toInt()
                        views.setTextColor(chronoId, highlightColor)
                    } else {
                        views.setViewVisibility(chronoId, android.view.View.GONE)
                    }
                }

                // Current time label and clock
                views.setTextViewText(R.id.tv_vertical_clock_label, if (isEn) "Current Time: " else "الساعة الآن: ")
                if (is24HourFormat) {
                    views.setCharSequence(R.id.tc_vertical_clock, "setFormat12Hour", "HH:mm")
                    views.setCharSequence(R.id.tc_vertical_clock, "setFormat24Hour", "HH:mm")
                } else {
                    views.setCharSequence(R.id.tc_vertical_clock, "setFormat12Hour", "hh:mm a")
                    views.setCharSequence(R.id.tc_vertical_clock, "setFormat24Hour", "hh:mm a")
                }

                // Theme & Mosque Background styling
                val showMosqueBg = com.example.util.MosqueBackgroundHelper.isEnabled(context)
                if (showMosqueBg) {
                    val mosqueInfo = com.example.util.MosqueBackgroundHelper.getMosqueForPrayer(nextPrayer.prayerKey)
                    views.setViewVisibility(R.id.iv_vertical_mosque_bg, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.iv_vertical_dark_overlay, android.view.View.VISIBLE)
                    views.setImageViewResource(R.id.iv_vertical_mosque_bg, mosqueInfo.drawableResId)
                    try {
                        views.setInt(R.id.widget_vertical_frame, "setBackgroundResource", R.drawable.bg_vertical_prayer_widget_dark)
                    } catch (_: Exception) {}

                    val normalColor = 0xFFFFFFFF.toInt()
                    val highlightColor = 0xFF4ADE80.toInt()

                    applyPrayerColors(views, nextIndex == 0, normalColor, highlightColor, R.id.tv_vertical_fajr_name, R.id.tv_vertical_fajr_time)
                    applyPrayerColors(views, nextIndex == 1, normalColor, highlightColor, R.id.tv_vertical_sunrise_name, R.id.tv_vertical_sunrise_time)
                    applyPrayerColors(views, nextIndex == 2, normalColor, highlightColor, R.id.tv_vertical_dhuhr_name, R.id.tv_vertical_dhuhr_time)
                    applyPrayerColors(views, nextIndex == 3, normalColor, highlightColor, R.id.tv_vertical_asr_name, R.id.tv_vertical_asr_time)
                    applyPrayerColors(views, nextIndex == 4, normalColor, highlightColor, R.id.tv_vertical_maghrib_name, R.id.tv_vertical_maghrib_time)
                    applyPrayerColors(views, nextIndex == 5, normalColor, highlightColor, R.id.tv_vertical_isha_name, R.id.tv_vertical_isha_time)

                    views.setTextColor(R.id.tv_vertical_clock_label, 0xFFE2E8F0.toInt())
                    views.setTextColor(R.id.tc_vertical_clock, 0xFFFFFFFF.toInt())
                } else {
                    views.setViewVisibility(R.id.iv_vertical_mosque_bg, android.view.View.GONE)
                    views.setViewVisibility(R.id.iv_vertical_dark_overlay, android.view.View.GONE)
                    if (isDark) {
                        try {
                            views.setInt(R.id.widget_vertical_frame, "setBackgroundResource", R.drawable.bg_vertical_prayer_widget_dark)
                        } catch (_: Exception) {}
                        val normalColor = 0xFFFFFFFF.toInt()
                        val highlightColor = 0xFF4ADE80.toInt()

                        applyPrayerColors(views, nextIndex == 0, normalColor, highlightColor, R.id.tv_vertical_fajr_name, R.id.tv_vertical_fajr_time)
                        applyPrayerColors(views, nextIndex == 1, normalColor, highlightColor, R.id.tv_vertical_sunrise_name, R.id.tv_vertical_sunrise_time)
                        applyPrayerColors(views, nextIndex == 2, normalColor, highlightColor, R.id.tv_vertical_dhuhr_name, R.id.tv_vertical_dhuhr_time)
                        applyPrayerColors(views, nextIndex == 3, normalColor, highlightColor, R.id.tv_vertical_asr_name, R.id.tv_vertical_asr_time)
                        applyPrayerColors(views, nextIndex == 4, normalColor, highlightColor, R.id.tv_vertical_maghrib_name, R.id.tv_vertical_maghrib_time)
                        applyPrayerColors(views, nextIndex == 5, normalColor, highlightColor, R.id.tv_vertical_isha_name, R.id.tv_vertical_isha_time)

                        views.setTextColor(R.id.tv_vertical_clock_label, 0xFFFFFFFF.toInt())
                        views.setTextColor(R.id.tc_vertical_clock, 0xFFFFFFFF.toInt())
                    } else {
                        try {
                            views.setInt(R.id.widget_vertical_frame, "setBackgroundResource", R.drawable.bg_vertical_prayer_widget_light)
                        } catch (_: Exception) {}
                        val normalNameColor = 0xFF334155.toInt()
                        val normalTimeColor = 0xFF0F172A.toInt()
                        val highlightColor = 0xFF059669.toInt()

                        applyPrayerColors(views, nextIndex == 0, normalNameColor, highlightColor, R.id.tv_vertical_fajr_name, R.id.tv_vertical_fajr_time, normalTimeColor)
                        applyPrayerColors(views, nextIndex == 1, normalNameColor, highlightColor, R.id.tv_vertical_sunrise_name, R.id.tv_vertical_sunrise_time, normalTimeColor)
                        applyPrayerColors(views, nextIndex == 2, normalNameColor, highlightColor, R.id.tv_vertical_dhuhr_name, R.id.tv_vertical_dhuhr_time, normalTimeColor)
                        applyPrayerColors(views, nextIndex == 3, normalNameColor, highlightColor, R.id.tv_vertical_asr_name, R.id.tv_vertical_asr_time, normalTimeColor)
                        applyPrayerColors(views, nextIndex == 4, normalNameColor, highlightColor, R.id.tv_vertical_maghrib_name, R.id.tv_vertical_maghrib_time, normalTimeColor)
                        applyPrayerColors(views, nextIndex == 5, normalNameColor, highlightColor, R.id.tv_vertical_isha_name, R.id.tv_vertical_isha_time, normalTimeColor)

                        views.setTextColor(R.id.tv_vertical_clock_label, 0xFF0F172A.toInt())
                        views.setTextColor(R.id.tc_vertical_clock, 0xFF0F172A.toInt())
                    }
                }

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
                views.setOnClickPendingIntent(R.id.widget_vertical_root, pendingIntent)

            } catch (e: Exception) {
                android.util.Log.e("VerticalPrayerWidget", "Failed to update widget views", e)
            } finally {
                try {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    android.util.Log.e("VerticalPrayerWidget", "Failed to update app widget", e)
                }
            }
        }

        private fun applyPrayerColors(
            views: RemoteViews,
            isNext: Boolean,
            normalNameColor: Int,
            highlightColor: Int,
            nameId: Int,
            timeId: Int,
            normalTimeColor: Int = normalNameColor
        ) {
            if (isNext) {
                views.setTextColor(nameId, highlightColor)
                views.setTextColor(timeId, highlightColor)
            } else {
                views.setTextColor(nameId, normalNameColor)
                views.setTextColor(timeId, normalTimeColor)
            }
        }

        private fun findNextPrayerIndex(timings: PrayerTimings): Int {
            val now = Calendar.getInstance()
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val prayerTimes = listOf(
                timings.fajr,
                timings.sunrise,
                timings.dhuhr,
                timings.asr,
                timings.maghrib,
                timings.isha
            )

            for (i in prayerTimes.indices) {
                val cleanTime = prayerTimes[i].take(5)
                try {
                    val parsed = sdf.parse(cleanTime)
                    if (parsed != null) {
                        val cal = Calendar.getInstance().apply {
                            time = now.time
                            val temp = Calendar.getInstance().apply { time = parsed }
                            set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (cal.timeInMillis > now.timeInMillis) {
                            return i
                        }
                    }
                } catch (_: Exception) {}
            }
            return 0
        }
    }
}
