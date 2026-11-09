package com.example.data.calculator

import com.example.data.model.PrayerOffsets
import com.example.data.model.PrayerTimings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object AdhanCalculator {

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        methodId: Int = 16,
        offsets: PrayerOffsets = PrayerOffsets(),
        timeZone: TimeZone = TimeZone.getDefault(),
        asrMethod: String = "standard"
    ): PrayerTimings {
        val calendar = Calendar.getInstance(timeZone).apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // TimeZone offset in hours
        val tzOffsetHours = timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        // 1. Julian Day
        val jd = julianDay(year, month, day)

        // 2. Solar Position (Declination & Equation of Time)
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val L = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(L))))
        val ra = Math.toDegrees(atan(cos(Math.toRadians(e)) * tan(Math.toRadians(L))))
        val raFixed = fixAngle(ra + (Math.floor(L / 180.0) * 180.0 - Math.floor(ra / 180.0) * 180.0))
        val eqTime = (q - raFixed) / 15.0

        // 3. Calculation Method Parameters (Fajr and Isha Angles)
        var fajrAngle = 18.0
        var ishaAngle = 17.0
        var isIshaInterval = false
        var ishaIntervalMinutes = 90

        var methodFajrOffset = 0
        var methodSunriseOffset = 0
        var methodDhuhrOffset = 0
        var methodAsrOffset = 0
        var methodMaghribOffset = 0
        var methodIshaOffset = 0

        when (methodId) {
            1 -> { // Karachi
                fajrAngle = 18.0
                ishaAngle = 18.0
            }
            2 -> { // ISNA
                fajrAngle = 15.0
                ishaAngle = 15.0
            }
            3 -> { // MWL
                fajrAngle = 18.0
                ishaAngle = 17.0
            }
            4 -> { // Umm Al Qura
                fajrAngle = 18.5
                isIshaInterval = true
                ishaIntervalMinutes = 90
            }
            5 -> { // Egyptian General Authority
                fajrAngle = 19.5
                ishaAngle = 17.5
            }
            12 -> { // UOIF France
                fajrAngle = 12.0
                ishaAngle = 12.0
            }
            13 -> { // Diyanet Turkey
                fajrAngle = 18.0
                ishaAngle = 17.0
            }
            16 -> { // Algerian Ministry of Religious Affairs (وزارة الشؤون الدينية والأوقاف - الجزائر)
                fajrAngle = 18.0
                ishaAngle = 17.0
                methodDhuhrOffset = 1
                methodAsrOffset = 0
                methodMaghribOffset = 2
                methodIshaOffset = 0
            }
        }

        // 4. Calculate Dhuhr (Midday)
        val dhuhrHours = 12.0 + tzOffsetHours - (longitude / 15.0) - eqTime

        // 5. Calculate Sunrise & Sunset (Maghrib)
        val sunriseHourAngle = hourAngle(-0.833, latitude, declination)
        val sunriseHours = dhuhrHours - (sunriseHourAngle / 15.0)
        val maghribHours = dhuhrHours + (sunriseHourAngle / 15.0)

        // 6. Calculate Fajr
        val fajrHourAngle = hourAngle(-fajrAngle, latitude, declination)
        val fajrHours = dhuhrHours - (fajrHourAngle / 15.0)

        // 7. Calculate Asr (Shafi'i / standard: shadow length = 1.0, Hanafi: shadow length = 2.0)
        val shadowFactor = if (asrMethod == "hanafi") 2.0 else 1.0
        val asrAngle = Math.toDegrees(atan(1.0 / (shadowFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = hourAngle(asrAngle, latitude, declination)
        val asrHours = dhuhrHours + (asrHourAngle / 15.0)

        // 8. Calculate Isha
        val ishaHours = if (isIshaInterval) {
            maghribHours + (ishaIntervalMinutes / 60.0)
        } else {
            val ishaHourAngle = hourAngle(-ishaAngle, latitude, declination)
            dhuhrHours + (ishaHourAngle / 15.0)
        }

        // Format to HH:mm string with manual offsets
        val sdf = SimpleDateFormat("HH:mm", Locale.US).apply {
            this.timeZone = timeZone
        }

        fun toFormattedTime(hoursDecimal: Double, offsetMinutes: Int): String {
            val totalMinutes = Math.round((hoursDecimal * 60) + offsetMinutes)
            var normMinutes = (totalMinutes % (24 * 60)).toInt()
            if (normMinutes < 0) normMinutes += 24 * 60
            val h = normMinutes / 60
            val m = normMinutes % 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        }

        return PrayerTimings(
            fajr = toFormattedTime(fajrHours, offsets.fajr + methodFajrOffset),
            sunrise = toFormattedTime(sunriseHours, offsets.sunrise + methodSunriseOffset),
            dhuhr = toFormattedTime(dhuhrHours, offsets.dhuhr + methodDhuhrOffset),
            asr = toFormattedTime(asrHours, offsets.asr + methodAsrOffset),
            maghrib = toFormattedTime(maghribHours, offsets.maghrib + methodMaghribOffset),
            isha = toFormattedTime(ishaHours, offsets.isha + methodIshaOffset)
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * Math.floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun hourAngle(angle: Double, lat: Double, decl: Double): Double {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val angleRad = Math.toRadians(angle)
        val cosH = (sin(angleRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH))
    }
}
