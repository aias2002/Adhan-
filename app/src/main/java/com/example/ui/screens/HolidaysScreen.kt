package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.util.AppLocalization
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ISLAMIC_HOLIDAYS_LIST
import com.example.data.model.IslamicHoliday
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

enum class HolidayStatus {
    PAST,          // Red color
    NEXT_UPCOMING, // Green color (glowing)
    LATER          // Dark / Black / Slate color
}

data class HolidayUiModel(
    val holiday: IslamicHoliday,
    val status: HolidayStatus,
    val daysRemaining: Int,
    val daysBadgeText: String,
    val displayHijriDateStr: String
)

private data class HolidayOccurrence(
    val holiday: IslamicHoliday,
    val thisYearDiff: Int,
    val daysUntilNext: Int,
    val nextYear: Int
)

@Composable
fun HolidaysScreen(
    hijriDate: String?,
    isDarkMode: Boolean,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = onOpenDrawer,
    appLanguage: String = "ar",
    hijriOffset: Int = 0,
    modifier: Modifier = Modifier
) {
    val isAr = appLanguage != "en"
    val backgroundColor = if (isDarkMode) Color(0xFF13171A) else Color(0xFFF7F4EE)

    // Current device date (user's timezone)
    var currentDate by remember { mutableStateOf(LocalDate.now(ZoneId.systemDefault())) }

    // Recalculate upon opening the screen and when day changes (midnight)
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000L)
            val now = LocalDate.now(ZoneId.systemDefault())
            if (now != currentDate) {
                currentDate = now
            }
        }
    }

    // Effective Hijri date for today using Umm al-Qura calendar (HijrahChronology)
    // with manual offset adjustment preserved
    val effectiveHijrahDate = remember(currentDate, hijriDate, hijriOffset) {
        val baseDate = HijrahChronology.INSTANCE.date(currentDate)
        val offset = if (hijriOffset != 0) {
            hijriOffset
        } else {
            extractManualOffset(hijriDate, baseDate)
        }
        if (offset != 0) {
            baseDate.plus(offset.toLong(), ChronoUnit.DAYS)
        } else {
            baseDate
        }
    }

    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current

    val displayHijriTodayStr = remember(hijriDate, effectiveHijrahDate, isAr, numeralSystem, appLanguage) {
        val raw = if (!hijriDate.isNullOrBlank()) {
            com.example.ui.util.AppLocalization.getLocalizedHijriDate(hijriDate, appLanguage)
        } else {
            val day = effectiveHijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = effectiveHijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val year = effectiveHijrahDate.get(ChronoField.YEAR)
            com.example.ui.util.AppLocalization.formatHijriDate(day, month, year, appLanguage)
        }
        if (isAr) raw.toConfiguredDigits(numeralSystem) else raw
    }

    val gregorianDateStr = remember(currentDate, appLanguage, numeralSystem) {
        val pattern = if (isAr) "EEEE d MMMM yyyy" else "EEEE, MMMM d, yyyy"
        val raw = SimpleDateFormat(pattern, Locale(if (isAr) "ar" else "en")).format(Date())
        if (isAr) raw.toConfiguredDigits(numeralSystem) else raw
    }

    // Calculate Islamic holidays:
    // 1. Build holiday dates in the current Hijri year.
    // 2. If already passed, calculate next occurrence in the next Hijri year.
    // 3. Find the holiday with minimum positive remaining days as the NEXT UPCOMING holiday.
    // 4. Sort upcoming events ascending by days remaining, followed by past events of current year.
    val holidayUiList = remember(effectiveHijrahDate, isAr, numeralSystem) {
        val currentHijriYear = effectiveHijrahDate.get(ChronoField.YEAR)
        val todayEpoch = effectiveHijrahDate.toEpochDay()

        val occurrences = ISLAMIC_HOLIDAYS_LIST.map { holiday ->
            val thisYearDate = safeHijrahDate(currentHijriYear, holiday.hijriMonth, holiday.hijriDay)
            val thisYearDiff = (thisYearDate.toEpochDay() - todayEpoch).toInt()

            val nextYearDate = safeHijrahDate(currentHijriYear + 1, holiday.hijriMonth, holiday.hijriDay)
            val nextYearDiff = (nextYearDate.toEpochDay() - todayEpoch).toInt()

            val daysUntilNext = if (thisYearDiff >= 0) thisYearDiff else nextYearDiff
            val targetYear = if (thisYearDiff >= 0) currentHijriYear else currentHijriYear + 1

            HolidayOccurrence(
                holiday = holiday,
                thisYearDiff = thisYearDiff,
                daysUntilNext = daysUntilNext,
                nextYear = targetYear
            )
        }

        // The holiday with the smallest positive days remaining is the Next Upcoming
        val nextUpcomingOccurrence = occurrences.minByOrNull { it.daysUntilNext }
        val nextUpcomingId = nextUpcomingOccurrence?.holiday?.id

        val upcomingList = mutableListOf<HolidayUiModel>()
        val pastList = mutableListOf<HolidayUiModel>()

        occurrences.forEach { occ ->
            val holiday = occ.holiday
            val targetHijriDate = if (occ.thisYearDiff >= 0) {
                safeHijrahDate(currentHijriYear, holiday.hijriMonth, holiday.hijriDay)
            } else {
                safeHijrahDate(currentHijriYear + 1, holiday.hijriMonth, holiday.hijriDay)
            }
            val gregDate = LocalDate.ofEpochDay(targetHijriDate.toEpochDay())
            val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale(if (isAr) "ar" else "en"))
            val rawGregStr = gregDate.format(formatter)
            val gregDateStr = if (isAr) rawGregStr.toConfiguredDigits(numeralSystem) else rawGregStr
            val rawHijriEvent = com.example.ui.util.AppLocalization.formatHijriEvent(holiday.hijriDay, holiday.hijriMonth, appLanguage)
            val formattedHijriDateStr = if (isAr) rawHijriEvent.toConfiguredDigits(numeralSystem) else rawHijriEvent

            val isNext = holiday.id == nextUpcomingId
            if (occ.thisYearDiff >= 0) {
                upcomingList.add(
                    HolidayUiModel(
                        holiday = holiday,
                        status = if (isNext) HolidayStatus.NEXT_UPCOMING else HolidayStatus.LATER,
                        daysRemaining = occ.thisYearDiff,
                        daysBadgeText = gregDateStr,
                        displayHijriDateStr = formattedHijriDateStr
                    )
                )
            } else {
                pastList.add(
                    HolidayUiModel(
                        holiday = holiday,
                        status = HolidayStatus.PAST,
                        daysRemaining = occ.thisYearDiff,
                        daysBadgeText = gregDateStr,
                        displayHijriDateStr = formattedHijriDateStr
                    )
                )
            }
        }

        // Sort upcoming ascending by remaining days
        upcomingList.sortBy { it.daysRemaining }

        // Sort past chronologically by month and day in current year
        pastList.sortBy { it.holiday.hijriMonth * 30 + it.holiday.hijriDay }

        if (upcomingList.isEmpty()) {
            occurrences.map { occ ->
                val isNext = occ.holiday.id == nextUpcomingId
                val targetHijriDate = safeHijrahDate(occ.nextYear, occ.holiday.hijriMonth, occ.holiday.hijriDay)
                val gregDate = LocalDate.ofEpochDay(targetHijriDate.toEpochDay())
                val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale(if (isAr) "ar" else "en"))
                val rawGregStr = gregDate.format(formatter)
                val gregDateStr = if (isAr) rawGregStr.toConfiguredDigits(numeralSystem) else rawGregStr
                val rawNextHijriEvent = com.example.ui.util.AppLocalization.formatHijriEvent(occ.holiday.hijriDay, occ.holiday.hijriMonth, appLanguage)
                val formattedNextHijri = if (isAr) "$rawNextHijriEvent ${occ.nextYear}".toConfiguredDigits(numeralSystem) else "$rawNextHijriEvent ${occ.nextYear}"

                HolidayUiModel(
                    holiday = occ.holiday,
                    status = if (isNext) HolidayStatus.NEXT_UPCOMING else HolidayStatus.LATER,
                    daysRemaining = occ.daysUntilNext,
                    daysBadgeText = gregDateStr,
                    displayHijriDateStr = formattedNextHijri
                )
            }.sortedBy { it.daysRemaining }
        } else {
            upcomingList + pastList
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Top Bar: Menu Button at Start (Right in Arabic RTL, Left in English LTR), Title Centered
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(onClick = onOpenSettings)
                        .testTag("btn_open_settings_holidays"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_settings_tasbih),
                        contentDescription = if (isAr) "الإعدادات" else "Settings",
                        tint = if (isDarkMode) Color(0xFFFFD60A) else Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = AppLocalization.getString("islamic_holidays", appLanguage),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = AppLocalization.getString("hijri_year_occasions", appLanguage),
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Dates Banner (Hijri + Gregorian Side-by-Side)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            // Hijri Date
                            Text(
                                text = displayHijriTodayStr,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            // Gregorian Date
                            Text(
                                text = gregorianDateStr,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of Islamic Holidays with distinct Color Badges
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(holidayUiList, key = { it.holiday.id }) { item ->
                    HolidayCard(
                        holiday = item.holiday,
                        status = item.status,
                        daysBadgeText = item.daysBadgeText,
                        displayHijriDateStr = item.displayHijriDateStr,
                        isDarkMode = isDarkMode,
                        isAr = isAr,
                        appLanguage = appLanguage
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun HolidayCard(
    holiday: IslamicHoliday,
    status: HolidayStatus,
    daysBadgeText: String,
    displayHijriDateStr: String,
    isDarkMode: Boolean,
    isAr: Boolean,
    appLanguage: String
) {
    // 1. Past: Red accent
    // 2. Next Upcoming: Vibrant Green accent + glowing border
    // 3. Later: Dark/Neutral Slate accent
    val statusColor = when (status) {
        HolidayStatus.PAST -> Color(0xFFE53935) // Red
        HolidayStatus.NEXT_UPCOMING -> Color(0xFF10B981) // Vibrant Emerald Green
        HolidayStatus.LATER -> if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF334155) // Dark / Slate
    }

    val badgeBgColor = when (status) {
        HolidayStatus.PAST -> Color(0xFFEF5350).copy(alpha = 0.16f)
        HolidayStatus.NEXT_UPCOMING -> Color(0xFF10B981).copy(alpha = 0.22f)
        HolidayStatus.LATER -> if (isDarkMode) Color(0xFF64748B).copy(alpha = 0.20f) else Color(0xFF0F172A).copy(alpha = 0.08f)
    }

    val cardBg = if (isDarkMode) Color(0xFF1E262E) else Color(0xFFFFFFFF)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (status == HolidayStatus.NEXT_UPCOMING) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (status == HolidayStatus.NEXT_UPCOMING) {
                    Modifier.border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (status == HolidayStatus.NEXT_UPCOMING) {
                        // Badge 1: Next Upcoming
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeBgColor
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isAr) "القادمة 🌟" else "Next 🌟",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        // Badge 2: Days remaining badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = daysBadgeText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    } else if (status == HolidayStatus.LATER) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeBgColor
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = daysBadgeText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    } else {
                        // PAST
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeBgColor
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isAr) "انقضى" else "Past",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }

                // Hijri date badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF00ACC1).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = displayHijriDateStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00ACC1),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (holiday.id.contains("eid")) Icons.Default.Star else Icons.Default.Mosque,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = AppLocalization.getString(holiday.id, appLanguage),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = AppLocalization.getString("${holiday.id}_desc", appLanguage),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Significance note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = AppLocalization.getString("${holiday.id}_sig", appLanguage),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun safeHijrahDate(year: Int, month: Int, day: Int): HijrahDate {
    val m = month.coerceIn(1, 12)
    var d = day.coerceIn(1, 30)
    while (d > 0) {
        try {
            return HijrahChronology.INSTANCE.date(year, m, d)
        } catch (_: Exception) {
            d--
        }
    }
    return HijrahChronology.INSTANCE.date(year, m, 1)
}

fun formatDaysRemaining(days: Int, isAr: Boolean): String {
    return when {
        days == 0 -> if (isAr) "اليوم" else "Today"
        days == 1 -> if (isAr) "غداً" else "Tomorrow"
        days == 2 -> if (isAr) "بعد يومين" else "In 2 days"
        days in 3..10 -> if (isAr) "بعد $days أيام" else "In $days days"
        else -> if (isAr) "بعد $days يوماً" else "In $days days"
    }
}

fun getHijriMonthNameAr(month: Int): String {
    return when (month) {
        1 -> "محرم"
        2 -> "صفر"
        3 -> "ربيع الأول"
        4 -> "ربيع الثاني"
        5 -> "جمادى الأولى"
        6 -> "جمادى الآخرة"
        7 -> "رجب"
        8 -> "شعبان"
        9 -> "رمضان"
        10 -> "شوال"
        11 -> "ذو القعدة"
        12 -> "ذو الحجة"
        else -> "محرم"
    }
}

fun getHijriMonthNameEn(month: Int): String {
    return when (month) {
        1 -> "Muharram"
        2 -> "Safar"
        3 -> "Rabi' al-Awwal"
        4 -> "Rabi' al-Thani"
        5 -> "Jumada al-Awwal"
        6 -> "Jumada al-Thani"
        7 -> "Rajab"
        8 -> "Sha'ban"
        9 -> "Ramadan"
        10 -> "Shawwal"
        11 -> "Dhu al-Qi'dah"
        12 -> "Dhu al-Hijjah"
        else -> "Muharram"
    }
}

fun formatHijriDate(hijrahDate: HijrahDate, isAr: Boolean): String {
    val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
    val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
    val year = hijrahDate.get(ChronoField.YEAR)
    return if (isAr) {
        "$day ${getHijriMonthNameAr(month)} $year هـ"
    } else {
        "$day ${getHijriMonthNameEn(month)} $year AH"
    }
}

private fun extractManualOffset(hijriDateStr: String?, baseHijrahDate: HijrahDate): Int {
    if (hijriDateStr.isNullOrBlank()) return 0
    val parsedDay = parseHijriDayNumber(hijriDateStr) ?: return 0
    val parsedMonth = parseHijriMonthNumber(hijriDateStr)
    val baseDay = baseHijrahDate.get(ChronoField.DAY_OF_MONTH)
    val baseMonth = baseHijrahDate.get(ChronoField.MONTH_OF_YEAR)
    if (parsedMonth == baseMonth) {
        val diff = parsedDay - baseDay
        if (diff in -3..3) return diff
    }
    return 0
}

private fun parseHijriMonthNumber(text: String?): Int {
    if (text == null) return 1
    return when {
        text.contains("محرم") || text.contains("Muharram") -> 1
        text.contains("صفر") || text.contains("Safar") -> 2
        text.contains("ربيع الأول") || text.contains("Rabi' al-Awwal") || text.contains("Rabi I") -> 3
        text.contains("ربيع الثاني") || text.contains("ربيع الآخر") || text.contains("Rabi II") -> 4
        text.contains("جمادى الأولى") || text.contains("Jumada I") -> 5
        text.contains("جمادى الآخرة") || text.contains("جمادى الثانية") || text.contains("Jumada II") -> 6
        text.contains("رجب") || text.contains("Rajab") -> 7
        text.contains("شعبان") || text.contains("Sha'ban") -> 8
        text.contains("رمضان") || text.contains("Ramadan") -> 9
        text.contains("شوال") || text.contains("Shawwal") -> 10
        text.contains("ذو القعدة") || text.contains("Dhu al-Qi'dah") -> 11
        text.contains("ذو الحجة") || text.contains("Dhu al-Hijjah") -> 12
        else -> 1
    }
}

private fun parseHijriDayNumber(text: String?): Int? {
    if (text.isNullOrBlank()) return null
    val normalized = text.map { ch ->
        when (ch) {
            '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
            '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
            else -> ch
        }
    }.joinToString("")
    val firstWord = normalized.trim().split(" ").firstOrNull { part -> part.all { it.isDigit() } }
    return firstWord?.toIntOrNull()
}

private fun toWesternArabicDigits(input: String): String {
    return input.map { ch ->
        when (ch) {
            '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
            '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
            else -> ch
        }
    }.joinToString("")
}

private fun String.toConfiguredDigits(numeralSystem: String): String {
    return com.example.ui.util.AppLocalization.formatDigits(this, numeralSystem)
}
