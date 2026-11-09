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
import com.example.util.AdhanScheduler

class EveningAdhkarWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_COUNT = "com.example.widget.EVENING_ACTION_COUNT"
        const val ACTION_NEXT = "com.example.widget.EVENING_ACTION_NEXT"

        private val EVENING_ADHKAR = listOf(
            Pair("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ", 1),
            Pair("اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ الْمَصِيرُ", 1),
            Pair("أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ", 3),
            Pair("بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ", 3),
            Pair("اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالآخِرَةِ", 1),
            Pair("حَسْبِيَ اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ", 7)
        )

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, EveningAdhkarWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(component)
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_adhkar_evening)
            val prefs = AdhanScheduler.getPrefs(context)

            val adhkarList = EVENING_ADHKAR

            var index = prefs.getInt("widget_evening_adhkar_index", 0)
            if (index >= adhkarList.size) index = 0

            var currentCount = prefs.getInt("widget_evening_adhkar_count", 0)
            val currentDhikr = adhkarList[index]
            val maxCount = currentDhikr.second

            val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
            val followSystem = appPrefs.getBoolean("follow_system_dark_mode", true)
            val isSystemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            val manualIsDark = appPrefs.getBoolean("is_dark_mode", false)
            val bgTheme = appPrefs.getString("app_background_theme", "parchment") ?: "parchment"
            val appStyle = appPrefs.getString("app_theme_style", "emerald") ?: "emerald"
            val isDark = (if (followSystem) isSystemDark else manualIsDark) || bgTheme == "dark_night" || appStyle.equals("night", ignoreCase = true)

            // Dynamic text color for optimal contrast and readability: Deep black (#000000) on light backgrounds, Pure white (#FFFFFF) on dark
            val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK

            if (isDark) {
                views.setInt(R.id.widget_adhkar_root, "setBackgroundResource", R.drawable.bg_compact_widget_dark)
                views.setInt(R.id.btn_widget_adhkar_count, "setBackgroundResource", R.drawable.bg_widget_button_dark)
                views.setInt(R.id.btn_widget_adhkar_next, "setBackgroundResource", R.drawable.bg_widget_button_dark)
            } else {
                views.setInt(R.id.widget_adhkar_root, "setBackgroundResource", R.drawable.bg_compact_widget)
                views.setInt(R.id.btn_widget_adhkar_count, "setBackgroundResource", R.drawable.bg_widget_button_light)
                views.setInt(R.id.btn_widget_adhkar_next, "setBackgroundResource", R.drawable.bg_widget_button_light)
            }

            views.setTextColor(R.id.tv_adhkar_widget_type, textColor)
            views.setTextColor(R.id.tv_adhkar_widget_counter, textColor)
            views.setTextColor(R.id.tv_adhkar_widget_text, textColor)
            views.setTextColor(R.id.btn_widget_adhkar_count, textColor)
            views.setTextColor(R.id.btn_widget_adhkar_next, textColor)

            val appLanguage = appPrefs.getString("app_language", "ar") ?: "ar"
            val isEn = appLanguage == "en"

            views.setTextViewText(
                R.id.tv_adhkar_widget_type,
                if (isEn) "Evening Adhkar 🌙" else "أذكار المساء 🌙"
            )
            views.setTextViewText(R.id.tv_adhkar_widget_text, currentDhikr.first)
            views.setTextViewText(
                R.id.tv_adhkar_widget_counter,
                if (isEn) "Count: $currentCount / $maxCount" else "التكرار: $currentCount / $maxCount"
            )
            views.setTextViewText(
                R.id.btn_widget_adhkar_count,
                if (isEn) "Count 👆" else "تسبيح (اضغط) 👆"
            )
            views.setTextViewText(
                R.id.btn_widget_adhkar_next,
                if (isEn) "Next ➡️" else "الذكر التالي ➡️"
            )

            val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            // Intent for Count Button
            val countIntent = Intent(context, EveningAdhkarWidgetProvider::class.java).apply {
                action = ACTION_COUNT
            }
            val countPendingIntent = PendingIntent.getBroadcast(context, 101, countIntent, pFlags)
            views.setOnClickPendingIntent(R.id.btn_widget_adhkar_count, countPendingIntent)

            // Intent for Next Button
            val nextIntent = Intent(context, EveningAdhkarWidgetProvider::class.java).apply {
                action = ACTION_NEXT
            }
            val nextPendingIntent = PendingIntent.getBroadcast(context, 102, nextIntent, pFlags)
            views.setOnClickPendingIntent(R.id.btn_widget_adhkar_next, nextPendingIntent)

            // Open app on background click
            val appIntent = Intent(context, MainActivity::class.java)
            val appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, pFlags)
            views.setOnClickPendingIntent(R.id.widget_adhkar_root, appPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = AdhanScheduler.getPrefs(context)
        val adhkarList = EVENING_ADHKAR

        var index = prefs.getInt("widget_evening_adhkar_index", 0)
        if (index >= adhkarList.size) index = 0
        var count = prefs.getInt("widget_evening_adhkar_count", 0)
        val maxCount = adhkarList[index].second

        when (intent.action) {
            ACTION_COUNT -> {
                count++
                if (count >= maxCount) {
                    count = 0
                    index = (index + 1) % adhkarList.size
                }
                prefs.edit()
                    .putInt("widget_evening_adhkar_index", index)
                    .putInt("widget_evening_adhkar_count", count)
                    .apply()
                updateAllWidgets(context)
            }
            ACTION_NEXT -> {
                index = (index + 1) % adhkarList.size
                prefs.edit()
                    .putInt("widget_evening_adhkar_index", index)
                    .putInt("widget_evening_adhkar_count", 0)
                    .apply()
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }
}
