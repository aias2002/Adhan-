package com.example.data.repository

import com.example.data.local.PrayerCacheDao
import com.example.data.local.PrayerCacheEntity
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerOffsets
import com.example.data.model.PrayerTimings
import com.example.data.remote.AladhanApiService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrayerRepository(
    private val apiService: AladhanApiService,
    private val cacheDao: PrayerCacheDao
) {
    private fun getSystemTimeZone(): TimeZone = TimeZone.getDefault()

    suspend fun fetchPrayerTimings(
        city: String,
        latitude: Double,
        longitude: Double,
        offsets: PrayerOffsets = PrayerOffsets(),
        method: Int = 16,
        asrMethod: String = "standard"
    ): Result<Pair<PrayerTimings, String?>> {
        val school = if (asrMethod == "hanafi") 1 else 0
        return try {
            val response = apiService.getPrayerTimings(latitude, longitude, method = method, school = school)
            if (response.code == 200 && response.data != null) {
                val raw = response.data.timings
                val adjusted = applyOffsets(raw, offsets)
                val hijri = response.data.dateInfo?.hijri?.let {
                    "${it.day} ${it.month?.ar ?: ""} ${it.year} هـ"
                }
 
                // Cache locally
                cacheDao.savePrayerCache(
                    PrayerCacheEntity(
                        cityKey = city,
                        fajr = adjusted.fajr,
                        sunrise = adjusted.sunrise,
                        dhuhr = adjusted.dhuhr,
                        asr = adjusted.asr,
                        maghrib = adjusted.maghrib,
                        isha = adjusted.isha,
                        dateHijri = hijri ?: ""
                    )
                )
                Result.success(Pair(adjusted, hijri))
            } else {
                val calculatedTimings = com.example.data.calculator.AdhanCalculator.calculatePrayerTimes(
                    latitude = latitude,
                    longitude = longitude,
                    methodId = method,
                    offsets = offsets,
                    asrMethod = asrMethod
                )
                Result.success(Pair(calculatedTimings, null))
            }
        } catch (e: Exception) {
            val calculatedTimings = com.example.data.calculator.AdhanCalculator.calculatePrayerTimes(
                latitude = latitude,
                longitude = longitude,
                methodId = method,
                offsets = offsets,
                asrMethod = asrMethod
            )
            Result.success(Pair(calculatedTimings, null))
        }
    }

    private fun applyOffsets(raw: PrayerTimings, offsets: PrayerOffsets): PrayerTimings {
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
        val sdf = SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = getSystemTimeZone()
        }
        return try {
            val date = sdf.parse(cleanTime) ?: return cleanTime
            val cal = Calendar.getInstance(getSystemTimeZone()).apply {
                time = date
                add(Calendar.MINUTE, offsetMinutes)
            }
            sdf.format(cal.time)
        } catch (_: Exception) {
            cleanTime
        }
    }

    fun calculateNextPrayer(timings: PrayerTimings): NextPrayerInfo {
        val unified = com.example.util.NextPrayerTracker.calculateNextPrayer(null, timings)
        val remMillis = com.example.util.NextPrayerTracker.getRemainingMillis(unified.targetTimeMillis)
        return NextPrayerInfo(
            nameAr = unified.nameAr,
            nameEn = unified.nameEn,
            formattedTime = unified.formattedTime,
            remainingMillis = remMillis
        )
    }
}
