package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.model.PrayerAdhanSettings
import com.example.data.model.PrayerTimings
import com.example.receiver.AdhanAlarmReceiver
import java.util.Calendar

object AdhanScheduler {
    private const val TAG = "AdhanScheduler"
    private const val PREFS_NAME = "adhan_scheduler_prefs"

    const val KEY_PRAYER_NAME = "prayer_name"
    const val KEY_PRAYER_KEY = "prayer_key"
    const val KEY_IS_SUNRISE = "is_sunrise"

    // Prayer Request Codes
    const val CODE_FAJR = 101
    const val CODE_SUNRISE = 102
    const val CODE_DHUHR = 103
    const val CODE_ASR = 104
    const val CODE_MAGHRIB = 105
    const val CODE_ISHA = 106

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAdhanConfig(
        context: Context,
        selectedSoundId: String,
        selectedFajrSoundId: String,
        customSoundUri: String?,
        overrideSilentMode: Boolean,
        useRoosterForSunrise: Boolean,
        prayerSettings: PrayerAdhanSettings,
        audioStreamType: String = "media"
    ) {
        getPrefs(context).edit()
            .putString("selected_sound_id", selectedSoundId)
            .putString("selected_fajr_sound_id", selectedFajrSoundId)
            .putString("custom_sound_uri", customSoundUri ?: "")
            .putBoolean("override_silent_mode", overrideSilentMode)
            .putBoolean("use_rooster_for_sunrise", useRoosterForSunrise)
            .putString("adhan_audio_stream_type", audioStreamType)
            .putBoolean("adhan_fajr", prayerSettings.fajr)
            .putBoolean("adhan_dhuhr", prayerSettings.dhuhr)
            .putBoolean("adhan_asr", prayerSettings.asr)
            .putBoolean("adhan_maghrib", prayerSettings.maghrib)
            .putBoolean("adhan_isha", prayerSettings.isha)
            .apply()
    }

    fun savePrayerTimings(context: Context, timings: PrayerTimings) {
        getPrefs(context).edit()
            .putString("time_fajr", timings.fajr.take(5))
            .putString("time_sunrise", timings.sunrise.take(5))
            .putString("time_dhuhr", timings.dhuhr.take(5))
            .putString("time_asr", timings.asr.take(5))
            .putString("time_maghrib", timings.maghrib.take(5))
            .putString("time_isha", timings.isha.take(5))
            .apply()
    }

    fun getSavedPrayerTimings(context: Context): PrayerTimings? {
        val prefs = getPrefs(context)
        val fajr = prefs.getString("time_fajr", null) ?: return null
        val sunrise = prefs.getString("time_sunrise", "06:30") ?: "06:30"
        val dhuhr = prefs.getString("time_dhuhr", "12:30") ?: "12:30"
        val asr = prefs.getString("time_asr", "15:45") ?: "15:45"
        val maghrib = prefs.getString("time_maghrib", "18:15") ?: "18:15"
        val isha = prefs.getString("time_isha", "19:30") ?: "19:30"
        return PrayerTimings(fajr, sunrise, dhuhr, asr, maghrib, isha)
    }

    fun scheduleAllPrayers(context: Context, timings: PrayerTimings) {
        savePrayerTimings(context, timings)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        scheduleSinglePrayer(context, alarmManager, "fajr", "الفجر", timings.fajr.take(5), CODE_FAJR, false)
        scheduleSinglePrayer(context, alarmManager, "sunrise", "الشروق", timings.sunrise.take(5), CODE_SUNRISE, true)
        scheduleSinglePrayer(context, alarmManager, "dhuhr", "الظهر", timings.dhuhr.take(5), CODE_DHUHR, false)
        scheduleSinglePrayer(context, alarmManager, "asr", "العصر", timings.asr.take(5), CODE_ASR, false)
        scheduleSinglePrayer(context, alarmManager, "maghrib", "المغرب", timings.maghrib.take(5), CODE_MAGHRIB, false)
        scheduleSinglePrayer(context, alarmManager, "isha", "العشاء", timings.isha.take(5), CODE_ISHA, false)
    }

    fun scheduleSinglePrayer(
        context: Context,
        alarmManager: AlarmManager,
        prayerKey: String,
        prayerNameAr: String,
        timeHHmm: String,
        requestCode: Int,
        isSunrise: Boolean
    ) {
        val cleanTime = timeHHmm.substringBefore(" ").trim()
        val parts = cleanTime.split(":")
        val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.trim()?.take(2)?.toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If today's time has already passed (with 15s grace), schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis() + 15000L) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val triggerMillis = calendar.timeInMillis

        val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            putExtra(KEY_PRAYER_KEY, prayerKey)
            putExtra(KEY_PRAYER_NAME, prayerNameAr)
            putExtra(KEY_IS_SUNRISE, isSunrise)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        // Show Intent when user clicks the alarm clock icon in status bar/settings
        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(context, 0, showIntent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled Adhan for $prayerNameAr at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission not granted: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } catch (err: Exception) {
                Log.e(TAG, "Fallback alarm scheduling failed: ${err.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule adhan for $prayerNameAr: ${e.message}")
        }
    }

    fun cancelAllPrayers(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val codes = listOf(CODE_FAJR, CODE_SUNRISE, CODE_DHUHR, CODE_ASR, CODE_MAGHRIB, CODE_ISHA)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        for (code in codes) {
            val intent = Intent(context, AdhanAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(context, code, intent, flags)
            if (pi != null) {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        }
    }

    fun getEffectivePrayerTimings(context: Context): PrayerTimings {
        getSavedPrayerTimings(context)?.let { return it }
        val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
        val schedulerPrefs = getPrefs(context)
        val lat = appPrefs.getFloat("saved_latitude", schedulerPrefs.getFloat("saved_latitude", 21.4225f)).toDouble()
        val lng = appPrefs.getFloat("saved_longitude", schedulerPrefs.getFloat("saved_longitude", 39.8262f)).toDouble()
        val calcMethodId = appPrefs.getInt("calculation_method_id", schedulerPrefs.getInt("calculation_method_id", 16))
        val asrMethod = appPrefs.getString("asr_method", schedulerPrefs.getString("asr_method", "standard")) ?: "standard"

        val offsets = com.example.data.model.PrayerOffsets(
            fajr = appPrefs.getInt("offset_fajr", 0),
            sunrise = appPrefs.getInt("offset_sunrise", 0),
            dhuhr = appPrefs.getInt("offset_dhuhr", 0),
            asr = appPrefs.getInt("offset_asr", 0),
            maghrib = appPrefs.getInt("offset_maghrib", 0),
            isha = appPrefs.getInt("offset_isha", 0)
        )
        val raw = com.example.data.calculator.AdhanCalculator.calculatePrayerTimes(
            latitude = lat,
            longitude = lng,
            date = java.util.Date(),
            methodId = calcMethodId,
            asrMethod = asrMethod
        )
        return applyOffsets(raw, offsets)
    }

    private fun applyOffsets(raw: PrayerTimings, offsets: com.example.data.model.PrayerOffsets): PrayerTimings {
        return PrayerTimings(
            fajr = addMinutes(raw.fajr, offsets.fajr),
            sunrise = addMinutes(raw.sunrise, offsets.sunrise),
            dhuhr = addMinutes(raw.dhuhr, offsets.dhuhr),
            asr = addMinutes(raw.asr, offsets.asr),
            maghrib = addMinutes(raw.maghrib, offsets.maghrib),
            isha = addMinutes(raw.isha, offsets.isha)
        )
    }

    private fun addMinutes(timeStr: String, offsetMinutes: Int): String {
        if (offsetMinutes == 0) return timeStr.take(5)
        val cleanTime = timeStr.take(5)
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
        return try {
            val date = sdf.parse(cleanTime) ?: return cleanTime
            val cal = java.util.Calendar.getInstance().apply {
                time = date
                add(java.util.Calendar.MINUTE, offsetMinutes)
            }
            sdf.format(cal.time)
        } catch (_: Exception) {
            cleanTime
        }
    }
}
