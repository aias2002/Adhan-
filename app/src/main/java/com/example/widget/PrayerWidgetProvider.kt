package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.repository.PrayerRepository
import com.example.util.AdhanScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerWidgetProvider : AppWidgetProvider() {

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
            val component = ComponentName(context, PrayerWidgetProvider::class.java)
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
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            
            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
            
            // Hide header if height is too low (e.g., < 100dp)
            if (minHeight > 0 && minHeight < 100) {
                views.setViewVisibility(R.id.layout_widget_header, android.view.View.GONE)
            } else {
                views.setViewVisibility(R.id.layout_widget_header, android.view.View.VISIBLE)
            }


            val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
            val schedulerPrefs = AdhanScheduler.getPrefs(context)

            val city = appPrefs.getString("saved_city_name", null)
                ?: schedulerPrefs.getString("saved_city_name", "الجزائر العاصمة")
                ?: "الجزائر العاصمة"
            val lat = appPrefs.getFloat("saved_latitude", schedulerPrefs.getFloat("saved_latitude", 36.7538f)).toDouble()
            val lng = appPrefs.getFloat("saved_longitude", schedulerPrefs.getFloat("saved_longitude", 3.0588f)).toDouble()
            val calcMethodId = appPrefs.getInt("calculation_method_id", schedulerPrefs.getInt("calculation_method_id", 16))
            val asrMethod = appPrefs.getString("asr_method", null)
                ?: schedulerPrefs.getString("asr_method", "standard")
                ?: "standard"

            val appLanguage = appPrefs.getString("app_language", "ar") ?: "ar"
            val savedHijri = appPrefs.getString("saved_hijri_date", null)
                ?: schedulerPrefs.getString("saved_hijri_date", "")
                ?: ""
            val displayHijri = if (savedHijri.isNotEmpty()) {
                com.example.ui.util.AppLocalization.getLocalizedHijriDate(savedHijri, appLanguage, short = true)
            } else {
                if (appLanguage == "en") "Hijri Date" else "التاريخ الهجري"
            }

            val isRamadan = savedHijri.contains("رمضان")

            val isEn = appLanguage == "en"
            val localizedCity = com.example.ui.util.AppLocalization.getLocalizedCityName(city, appLanguage)
            val timings = AdhanScheduler.getEffectivePrayerTimings(context)

            val imsakTime = calculateImsakTime(timings.fajr)

            val dateFormat = if (isEn) {
                SimpleDateFormat("dd MMMM yyyy", Locale.US)
            } else {
                SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
            }
            val displayGregorian = dateFormat.format(Date())

            // Update views
            val isDark = appPrefs.getBoolean("is_dark_mode", false)
            if (isDark) {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_compact_widget_dark)
                views.setTextColor(R.id.tv_widget_city, 0xFF4ADE80.toInt())
                views.setTextColor(R.id.tv_widget_gregorian, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_hijri, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_next_prayer_title, 0xFF4ADE80.toInt())

                views.setTextColor(R.id.tv_widget_imsak_title, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_imsak, 0xFF4ADE80.toInt())

                views.setTextColor(R.id.tv_widget_fajr_title, 0xFF4ADE80.toInt())
                views.setTextColor(R.id.tv_widget_fajr, 0xFF4ADE80.toInt())

                views.setTextColor(R.id.tv_widget_sunrise_title, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_sunrise, 0xFFE2E8F0.toInt())

                views.setTextColor(R.id.tv_widget_dhuhr_title, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_dhuhr, 0xFFE2E8F0.toInt())

                views.setTextColor(R.id.tv_widget_asr_title, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_asr, 0xFFE2E8F0.toInt())

                views.setTextColor(R.id.tv_widget_maghrib_title, 0xFF4ADE80.toInt())
                views.setTextColor(R.id.tv_widget_maghrib, 0xFF4ADE80.toInt())

                views.setTextColor(R.id.tv_widget_isha_title, 0xFF94A3B8.toInt())
                views.setTextColor(R.id.tv_widget_isha, 0xFFE2E8F0.toInt())
            } else {
                views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.bg_compact_widget)
                views.setTextColor(R.id.tv_widget_city, 0xFF1E4A38.toInt())
                views.setTextColor(R.id.tv_widget_gregorian, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_hijri, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_next_prayer_title, 0xFF1E4A38.toInt())

                views.setTextColor(R.id.tv_widget_imsak_title, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_imsak, 0xFF1E4A38.toInt())

                views.setTextColor(R.id.tv_widget_fajr_title, 0xFF1E4A38.toInt())
                views.setTextColor(R.id.tv_widget_fajr, 0xFF1E4A38.toInt())

                views.setTextColor(R.id.tv_widget_sunrise_title, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_sunrise, 0xFF3A4A42.toInt())

                views.setTextColor(R.id.tv_widget_dhuhr_title, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_dhuhr, 0xFF3A4A42.toInt())

                views.setTextColor(R.id.tv_widget_asr_title, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_asr, 0xFF3A4A42.toInt())

                views.setTextColor(R.id.tv_widget_maghrib_title, 0xFF1E4A38.toInt())
                views.setTextColor(R.id.tv_widget_maghrib, 0xFF1E4A38.toInt())

                views.setTextColor(R.id.tv_widget_isha_title, 0xFF5C6B63.toInt())
                views.setTextColor(R.id.tv_widget_isha, 0xFF3A4A42.toInt())
            }

            val is24HourFormat = appPrefs.getBoolean("is_24_hour_format", true)
            val numeralSystem = appPrefs.getString("numeral_system", "western") ?: "western"

            views.setTextViewText(R.id.tv_widget_city, "📍 $localizedCity")
            views.setTextViewText(R.id.tv_widget_gregorian, displayGregorian)
            views.setTextViewText(R.id.tv_widget_hijri, displayHijri)
            views.setTextViewText(R.id.tv_widget_imsak_title, if (isEn) "Imsak" else "الإمساك")
            views.setTextViewText(R.id.tv_widget_imsak, com.example.ui.util.AppLocalization.formatTime(imsakTime, is24HourFormat, appLanguage, numeralSystem))
            views.setTextViewText(R.id.tv_widget_fajr_title, if (isEn) "Fajr" else "الفجر")
            views.setTextViewText(R.id.tv_widget_fajr, com.example.ui.util.AppLocalization.formatTime(timings.fajr, is24HourFormat, appLanguage, numeralSystem))
            views.setTextViewText(R.id.tv_widget_sunrise_title, if (isEn) "Sunrise" else "الشروق")
            views.setTextViewText(R.id.tv_widget_sunrise, com.example.ui.util.AppLocalization.formatTime(timings.sunrise, is24HourFormat, appLanguage, numeralSystem))
            views.setTextViewText(R.id.tv_widget_dhuhr_title, if (isEn) "Dhuhr" else "الظهر")
            views.setTextViewText(R.id.tv_widget_dhuhr, com.example.ui.util.AppLocalization.formatTime(timings.dhuhr, is24HourFormat, appLanguage, numeralSystem))
            views.setTextViewText(R.id.tv_widget_asr_title, if (isEn) "Asr" else "العصر")
            views.setTextViewText(R.id.tv_widget_asr, com.example.ui.util.AppLocalization.formatTime(timings.asr, is24HourFormat, appLanguage, numeralSystem))
            views.setTextViewText(R.id.tv_widget_isha_title, if (isEn) "Isha" else "العشاء")
            views.setTextViewText(R.id.tv_widget_isha, com.example.ui.util.AppLocalization.formatTime(timings.isha, is24HourFormat, appLanguage, numeralSystem))

            // Conditional Ramadan visibility for Imsak & Iftar
            if (isRamadan) {
                views.setViewVisibility(R.id.layout_widget_imsak, android.view.View.VISIBLE)
                views.setTextViewText(R.id.tv_widget_maghrib_title, if (isEn) "Maghrib (Iftar)" else "المغرب (الإفطار)")
            } else {
                views.setViewVisibility(R.id.layout_widget_imsak, android.view.View.GONE)
                views.setTextViewText(R.id.tv_widget_maghrib_title, if (isEn) "Maghrib" else "المغرب")
            }
            views.setTextViewText(R.id.tv_widget_maghrib, com.example.ui.util.AppLocalization.formatTime(timings.maghrib, is24HourFormat, appLanguage, numeralSystem))

            views.setTextViewText(
                R.id.tv_widget_next_prayer_title,
                if (isEn) "Today's Prayer Times" else "مواقيت الصلاة اليوم"
            )
            views.setViewVisibility(R.id.tv_widget_countdown, android.view.View.GONE)

            // Click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, pFlags)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun calculateImsakTime(fajrTime: String): String {
            return try {
                val clean = fajrTime.trim().split(" ")[0]
                val parts = clean.split(":")
                var h = parts[0].toInt()
                var m = parts[1].toInt() - 15
                if (m < 0) {
                    m += 60
                    h = (h - 1 + 24) % 24
                }
                String.format(Locale.getDefault(), "%02d:%02d", h, m)
            } catch (_: Exception) {
                fajrTime
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}
