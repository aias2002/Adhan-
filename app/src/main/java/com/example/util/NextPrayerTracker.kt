package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.data.calculator.AdhanCalculator
import com.example.data.model.PrayerOffsets
import com.example.data.model.PrayerTimings
import com.example.receiver.NextPrayerTransitionReceiver
import com.example.widget.CompactPrayerWidgetProvider
import com.example.widget.EveningAdhkarWidgetProvider
import com.example.widget.MorningAdhkarWidgetProvider
import com.example.widget.NextPrayerWidgetProvider
import com.example.widget.PrayerWidgetProvider
import com.example.widget.VerticalPrayerWidgetProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Unified Next Prayer Model (Requirement 1)
 */
data class UnifiedNextPrayer(
    val prayerKey: String,         // "fajr", "dhuhr", "asr", "maghrib", "isha"
    val nameAr: String,            // "الفجر", "الظهر", "العصر", "المغرب", "العشاء"
    val nameEn: String,            // "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"
    val targetTimeMillis: Long,    // Exact absolute epoch milliseconds of next prayer
    val previousTimeMillis: Long,  // Exact absolute epoch milliseconds of previous prayer
    val formattedTime: String,     // e.g. "05:24"
    val isTomorrow: Boolean = false,
    val previousPrayerKey: String = "isha",
    val previousNameAr: String = "العشاء",
    val previousNameEn: String = "Isha"
)

/**
 * Single Source of Truth for next prayer calculation and synchronization across:
 * 1. In-app countdown timer
 * 2. Status bar & Lock screen notification (Chronometer)
 * 3. Home screen widgets (RemoteViews Chronometer)
 */
object NextPrayerTracker {
    private const val TAG = "NextPrayerTracker"
    const val ACTION_PRAYER_TRANSITION = "com.example.util.ACTION_PRAYER_TRANSITION"
    private const val REQUEST_CODE_TRANSITION = 9901

    private const val PREFS_NAME = "next_prayer_tracker_prefs"
    private const val KEY_PRAYER_KEY = "unified_next_prayer_key"
    private const val KEY_NAME_AR = "unified_next_name_ar"
    private const val KEY_NAME_EN = "unified_next_name_en"
    private const val KEY_TARGET_MILLIS = "unified_next_target_millis"
    private const val KEY_PREV_MILLIS = "unified_next_prev_millis"
    private const val KEY_FORMATTED_TIME = "unified_next_formatted_time"
    private const val KEY_IS_TOMORROW = "unified_next_is_tomorrow"
    private const val KEY_PREV_KEY = "unified_next_prev_key"
    private const val KEY_PREV_NAME_AR = "unified_next_prev_name_ar"
    private const val KEY_PREV_NAME_EN = "unified_next_prev_name_en"

    @Volatile
    private var cachedNextPrayer: UnifiedNextPrayer? = null

    /**
     * Unified rounding and formatting function (Requirement 7).
     * Rule: Floor division matching Android native Chronometer countdown:
     * seconds = (targetTimeMillis - nowMillis) / 1000
     * This guarantees 0 difference between in-app, notification, and widget chronometers.
     */
    fun formatRemainingTime(remainingMillis: Long): String {
        val totalSeconds = maxOf(0L, remainingMillis / 1000L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getRemainingMillis(targetMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long {
        return maxOf(0L, targetMillis - nowMillis)
    }

    /**
     * Returns the current UnifiedNextPrayer from cache, prefs, or calculates fresh if expired.
     */
    fun getNextPrayer(context: Context, forceRefresh: Boolean = false): UnifiedNextPrayer {
        val nowMs = System.currentTimeMillis()
        val cached = cachedNextPrayer
        if (!forceRefresh && cached != null && cached.targetTimeMillis > nowMs) {
            return cached
        }

        if (!forceRefresh) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val targetMs = prefs.getLong(KEY_TARGET_MILLIS, 0L)
            if (targetMs > nowMs) {
                val restored = UnifiedNextPrayer(
                    prayerKey = prefs.getString(KEY_PRAYER_KEY, "fajr") ?: "fajr",
                    nameAr = prefs.getString(KEY_NAME_AR, "الفجر") ?: "الفجر",
                    nameEn = prefs.getString(KEY_NAME_EN, "Fajr") ?: "Fajr",
                    targetTimeMillis = targetMs,
                    previousTimeMillis = prefs.getLong(KEY_PREV_MILLIS, targetMs - 3600000L),
                    formattedTime = prefs.getString(KEY_FORMATTED_TIME, "05:00") ?: "05:00",
                    isTomorrow = prefs.getBoolean(KEY_IS_TOMORROW, false),
                    previousPrayerKey = prefs.getString(KEY_PREV_KEY, "isha") ?: "isha",
                    previousNameAr = prefs.getString(KEY_PREV_NAME_AR, "العشاء") ?: "العشاء",
                    previousNameEn = prefs.getString(KEY_PREV_NAME_EN, "Isha") ?: "Isha"
                )
                cachedNextPrayer = restored
                return restored
            }
        }

        val timings = AdhanScheduler.getEffectivePrayerTimings(context)
        val computed = calculateNextPrayer(context, timings, nowMs)
        saveNextPrayer(context, computed)
        cachedNextPrayer = computed
        return computed
    }

    /**
     * Pure calculation of next prayer target based on shared effective timings and current time.
     */
    fun calculateNextPrayer(
        context: Context? = null,
        timings: PrayerTimings,
        nowMs: Long = System.currentTimeMillis()
    ): UnifiedNextPrayer {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMs }

        fun parsePrayerCal(timeStr: String, dayOffset: Int = 0): Calendar {
            val clean = timeStr.trim().take(5)
            val date = try { sdf.parse(clean) } catch (_: Exception) { null }
            return Calendar.getInstance().apply {
                timeInMillis = nowMs
                if (dayOffset != 0) add(Calendar.DAY_OF_YEAR, dayOffset)
                if (date != null) {
                    val temp = Calendar.getInstance().apply { time = date }
                    set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                } else {
                    set(Calendar.HOUR_OF_DAY, 5)
                    set(Calendar.MINUTE, 0)
                }
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val calFajr = parsePrayerCal(timings.fajr, 0)
        val calDhuhr = parsePrayerCal(timings.dhuhr, 0)
        val calAsr = parsePrayerCal(timings.asr, 0)
        val calMaghrib = parsePrayerCal(timings.maghrib, 0)
        val calIsha = parsePrayerCal(timings.isha, 0)

        return when {
            nowMs < calFajr.timeInMillis -> {
                // Before today's Fajr
                val calYesterdayIsha = parsePrayerCal(timings.isha, -1)
                UnifiedNextPrayer(
                    prayerKey = "fajr",
                    nameAr = "الفجر",
                    nameEn = "Fajr",
                    targetTimeMillis = calFajr.timeInMillis,
                    previousTimeMillis = calYesterdayIsha.timeInMillis,
                    formattedTime = timings.fajr.take(5),
                    isTomorrow = false,
                    previousPrayerKey = "isha",
                    previousNameAr = "العشاء",
                    previousNameEn = "Isha"
                )
            }
            nowMs < calDhuhr.timeInMillis -> {
                // Between Fajr and Dhuhr
                UnifiedNextPrayer(
                    prayerKey = "dhuhr",
                    nameAr = "الظهر",
                    nameEn = "Dhuhr",
                    targetTimeMillis = calDhuhr.timeInMillis,
                    previousTimeMillis = calFajr.timeInMillis,
                    formattedTime = timings.dhuhr.take(5),
                    isTomorrow = false,
                    previousPrayerKey = "fajr",
                    previousNameAr = "الفجر",
                    previousNameEn = "Fajr"
                )
            }
            nowMs < calAsr.timeInMillis -> {
                // Between Dhuhr and Asr
                UnifiedNextPrayer(
                    prayerKey = "asr",
                    nameAr = "العصر",
                    nameEn = "Asr",
                    targetTimeMillis = calAsr.timeInMillis,
                    previousTimeMillis = calDhuhr.timeInMillis,
                    formattedTime = timings.asr.take(5),
                    isTomorrow = false,
                    previousPrayerKey = "dhuhr",
                    previousNameAr = "الظهر",
                    previousNameEn = "Dhuhr"
                )
            }
            nowMs < calMaghrib.timeInMillis -> {
                // Between Asr and Maghrib
                UnifiedNextPrayer(
                    prayerKey = "maghrib",
                    nameAr = "المغرب",
                    nameEn = "Maghrib",
                    targetTimeMillis = calMaghrib.timeInMillis,
                    previousTimeMillis = calAsr.timeInMillis,
                    formattedTime = timings.maghrib.take(5),
                    isTomorrow = false,
                    previousPrayerKey = "asr",
                    previousNameAr = "العصر",
                    previousNameEn = "Asr"
                )
            }
            nowMs < calIsha.timeInMillis -> {
                // Between Maghrib and Isha
                UnifiedNextPrayer(
                    prayerKey = "isha",
                    nameAr = "العشاء",
                    nameEn = "Isha",
                    targetTimeMillis = calIsha.timeInMillis,
                    previousTimeMillis = calMaghrib.timeInMillis,
                    formattedTime = timings.isha.take(5),
                    isTomorrow = false,
                    previousPrayerKey = "maghrib",
                    previousNameAr = "المغرب",
                    previousNameEn = "Maghrib"
                )
            }
            else -> {
                // All 5 prayers of today have passed -> Tomorrow's Fajr
                val tomorrowFajr = calculateTomorrowFajr(context, timings, calFajr)
                UnifiedNextPrayer(
                    prayerKey = "fajr",
                    nameAr = "الفجر",
                    nameEn = "Fajr",
                    targetTimeMillis = tomorrowFajr.timeInMillis,
                    previousTimeMillis = calIsha.timeInMillis,
                    formattedTime = String.format(Locale.US, "%02d:%02d", tomorrowFajr.get(Calendar.HOUR_OF_DAY), tomorrowFajr.get(Calendar.MINUTE)),
                    isTomorrow = true,
                    previousPrayerKey = "isha",
                    previousNameAr = "العشاء",
                    previousNameEn = "Isha"
                )
            }
        }
    }

    private fun calculateTomorrowFajr(context: Context?, todayTimings: PrayerTimings, todayCalFajr: Calendar): Calendar {
        if (context == null) {
            return Calendar.getInstance().apply {
                timeInMillis = todayCalFajr.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return try {
            val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
            val schedulerPrefs = AdhanScheduler.getPrefs(context)
            val lat = appPrefs.getFloat("saved_latitude", schedulerPrefs.getFloat("saved_latitude", 21.4225f)).toDouble()
            val lng = appPrefs.getFloat("saved_longitude", schedulerPrefs.getFloat("saved_longitude", 39.8262f)).toDouble()
            val methodId = appPrefs.getInt("calculation_method_id", schedulerPrefs.getInt("calculation_method_id", 16))
            val asrMethod = appPrefs.getString("asr_method", schedulerPrefs.getString("asr_method", "standard")) ?: "standard"
            val offsetFajr = appPrefs.getInt("offset_fajr", 0)

            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowRaw = AdhanCalculator.calculatePrayerTimes(
                latitude = lat,
                longitude = lng,
                date = tomorrow.time,
                methodId = methodId,
                asrMethod = asrMethod
            )
            val cleanFajr = tomorrowRaw.fajr.take(5)

            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val parsed = sdf.parse(cleanFajr)
            if (parsed != null) {
                val cal = Calendar.getInstance().apply {
                    time = tomorrow.time
                    val temp = Calendar.getInstance().apply { time = parsed }
                    set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (offsetFajr != 0) add(Calendar.MINUTE, offsetFajr)
                }
                cal
            } else {
                Calendar.getInstance().apply {
                    timeInMillis = todayCalFajr.timeInMillis
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        } catch (_: Exception) {
            Calendar.getInstance().apply {
                timeInMillis = todayCalFajr.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun saveNextPrayer(context: Context, prayer: UnifiedNextPrayer) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PRAYER_KEY, prayer.prayerKey)
            .putString(KEY_NAME_AR, prayer.nameAr)
            .putString(KEY_NAME_EN, prayer.nameEn)
            .putLong(KEY_TARGET_MILLIS, prayer.targetTimeMillis)
            .putLong(KEY_PREV_MILLIS, prayer.previousTimeMillis)
            .putString(KEY_FORMATTED_TIME, prayer.formattedTime)
            .putBoolean(KEY_IS_TOMORROW, prayer.isTomorrow)
            .putString(KEY_PREV_KEY, prayer.previousPrayerKey)
            .putString(KEY_PREV_NAME_AR, prayer.previousNameAr)
            .putString(KEY_PREV_NAME_EN, prayer.previousNameEn)
            .apply()
    }

    /**
     * Requirement 5: Schedule exact transition alarm right at targetTimeMillis.
     */
    fun scheduleTransitionAlarm(context: Context, targetMillis: Long) {
        if (targetMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NextPrayerTransitionReceiver::class.java).apply {
            action = ACTION_PRAYER_TRANSITION
            putExtra("target_millis", targetMillis)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_TRANSITION, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled next prayer transition alarm at $targetMillis (${Date(targetMillis)})")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission rejected, using fallback: ${e.message}")
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingIntent)
            } catch (_: Exception) {}
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule transition alarm: ${e.message}")
        }
    }

    /**
     * Synchronize all three interfaces (In-App, Notification, Widgets) from the Single Source of Truth.
     */
    fun syncAll(context: Context) {
        try {
            val nextPrayer = getNextPrayer(context, forceRefresh = true)
            Log.d(TAG, "syncAll: Next prayer is ${nextPrayer.nameAr} (${nextPrayer.formattedTime}) at ${Date(nextPrayer.targetTimeMillis)}")

            // 1. Update Notification
            PrayerNotificationHelper.updateNotification(context)

            // 2. Update all widgets
            NextPrayerWidgetProvider.updateAllWidgets(context)
            CompactPrayerWidgetProvider.updateAllWidgets(context)
            PrayerWidgetProvider.updateAllWidgets(context)
            VerticalPrayerWidgetProvider.updateAllWidgets(context)
            MorningAdhkarWidgetProvider.updateAllWidgets(context)
            EveningAdhkarWidgetProvider.updateAllWidgets(context)

            // 3. Schedule transition alarm for next prayer
            scheduleTransitionAlarm(context, nextPrayer.targetTimeMillis)
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncAll: ${e.message}", e)
        }
    }
}
