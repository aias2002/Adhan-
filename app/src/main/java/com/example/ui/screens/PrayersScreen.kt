package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ISLAMIC_HOLIDAYS
import com.example.data.model.IslamicEvent
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerTimings
import com.example.data.model.calculateImsakTime
import com.example.ui.components.AutoSizingText
import com.example.ui.theme.CairoFontFamily
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    cityName: String,
    countryName: String,
    hijriDate: String?,
    prayerTimings: PrayerTimings?,
    nextPrayer: NextPrayerInfo?,
    countdownText: String,
    currentTimeText: String,
    isLoading: Boolean,
    isAdhanPlaying: Boolean = false,
    onOpenLocationModal: () -> Unit,
    onOpenAdhanModal: () -> Unit,
    onOpenHijriModal: () -> Unit = {},
    onStopAdhan: () -> Unit = {},
    appLanguage: String = "ar",
    is24HourFormat: Boolean = true,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
            isRefreshing = false
        },
        modifier = modifier.fillMaxSize()
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val showMosqueBg = com.example.util.MosqueBackgroundHelper.isEnabled(context)
        val activePrayer = nextPrayer?.nameEn ?: nextPrayer?.nameAr ?: "dhuhr"
        val mosqueInfo = remember(activePrayer) { com.example.util.MosqueBackgroundHelper.getMosqueForPrayer(activePrayer) }

        Box(modifier = Modifier.fillMaxSize()) {
            // Mosque Artwork Overlay Background
            if (showMosqueBg) {
                Image(
                    painter = painterResource(id = mosqueInfo.drawableResId),
                    contentDescription = mosqueInfo.nameAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    alpha = 0.18f
                )
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Location Header Card
            LocationHeaderCard(
                cityName = cityName,
                countryName = countryName,
                hijriDate = hijriDate,
                onOpenLocationModal = onOpenLocationModal,
                onOpenAdhanModal = onOpenAdhanModal,
                onOpenHijriModal = onOpenHijriModal
            )

            // Playing Adhan Banner (Illuminated High Contrast Emerald & Gold)
            AnimatedVisibility(visible = isAdhanPlaying) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF064E3B)
                    ),
                    border = BorderStroke(2.dp, Color(0xFFFDE047)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE047)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = Color(0xFF064E3B),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "الأذان يعمل الآن...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "حي على الصلاة • حي على الفلاح",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF08A)
                                )
                            }
                        }

                        Button(
                            onClick = onStopAdhan,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "إيقاف",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إيقاف",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current

            // Countdown Hero Section
            CountdownHeroCard(
                nextPrayer = nextPrayer,
                prayerTimings = prayerTimings,
                countdownText = countdownText,
                currentTimeText = com.example.ui.util.AppLocalization.formatDigits(currentTimeText, numeralSystem),
                isLoading = isLoading
            )

            // Special Ramadan Imsak & Iftar Card (Appears only during Ramadan)
            val isRamadan = hijriDate?.contains("رمضان", ignoreCase = true) == true
            if (prayerTimings != null && isRamadan) {
                RamadanImsakIftarCard(
                    fajrTime = com.example.ui.util.AppLocalization.formatDigits(prayerTimings.fajr, numeralSystem),
                    maghribTime = com.example.ui.util.AppLocalization.formatDigits(prayerTimings.maghrib, numeralSystem)
                )
            }

            // Grid of 6 Prayers
            if (prayerTimings != null) {
                val prayerList = listOf(
                    PrayerItemData("Fajr", "الفجر", prayerTimings.fajr),
                    PrayerItemData("Sunrise", "الشروق", prayerTimings.sunrise),
                    PrayerItemData("Dhuhr", "الظهر", prayerTimings.dhuhr),
                    PrayerItemData("Asr", "العصر", prayerTimings.asr),
                    PrayerItemData("Maghrib", "المغرب", prayerTimings.maghrib),
                    PrayerItemData("Isha", "العشاء", prayerTimings.isha)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    prayerList.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                val isNext = nextPrayer?.nameEn.equals(item.key, ignoreCase = true)
                                Box(modifier = Modifier.weight(1f)) {
                                    PrayerCard(
                                        title = item.nameAr,
                                        time = item.time.take(5),
                                        isNext = isNext,
                                        is24HourFormat = is24HourFormat,
                                        appLanguage = appLanguage
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            IslamicEventsCalendarCard(hijriDate = hijriDate)
        }
    }
}
}


@Composable
private fun RamadanImsakIftarCard(
    fajrTime: String,
    maghribTime: String
) {
    val imsakTime = remember(fajrTime) { calculateImsakTime(fajrTime) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "مواقيت الإمساك والإفطار المباركة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "شهر رمضان المبارك",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Imsak Pill
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🌙 الإمساك",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = imsakTime,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Iftar Pill
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🌅 الإفطار (المغرب)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = maghribTime.take(5),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class IslamicHolidayStatus {
    PAST,
    NEXT,
    UPCOMING
}

private fun parseHijriMonthNumber(monthName: String): Int {
    return when {
        monthName.contains("محرم") -> 1
        monthName.contains("صفر") -> 2
        monthName.contains("ربيع الأول") || monthName.contains("ربيع الاول") || monthName.contains("ربيع 1") -> 3
        monthName.contains("ربيع الثاني") || monthName.contains("ربيع الآخر") || monthName.contains("ربيع الاخر") || monthName.contains("ربيع 2") -> 4
        monthName.contains("جمادى الأول") || monthName.contains("جمادى الأولى") || monthName.contains("جمادى 1") -> 5
        monthName.contains("جمادى الآخر") || monthName.contains("جمادى الآخرة") || monthName.contains("جمادى الثاني") || monthName.contains("جمادى 2") -> 6
        monthName.contains("رجب") -> 7
        monthName.contains("شعبان") -> 8
        monthName.contains("رمضان") -> 9
        monthName.contains("شوال") -> 10
        monthName.contains("ذو القعدة") || monthName.contains("ذي القعدة") -> 11
        monthName.contains("ذو الحجة") || monthName.contains("ذي الحجة") -> 12
        else -> 4
    }
}

private fun parseHijriDayAndMonth(hijriDateStr: String?): Pair<Int, Int>? {
    if (hijriDateStr.isNullOrBlank()) return null
    val parts = hijriDateStr.trim().split(" ")
    if (parts.isNotEmpty()) {
        val day = parts[0].filter { it.isDigit() }.toIntOrNull() ?: 1
        val month = parseHijriMonthNumber(hijriDateStr)
        return Pair(day, month)
    }
    return null
}

@Composable
private fun IslamicEventsCalendarCard(hijriDate: String?) {
    var expanded by remember { mutableStateOf(false) }

    val parsedDate = remember(hijriDate) { parseHijriDayAndMonth(hijriDate) }
    val currDay = parsedDate?.first ?: 1
    val currMonth = parsedDate?.second ?: 4

    val isPastPredicate = { ev: IslamicEvent ->
        if (ev.hijriMonth < currMonth) true
        else if (ev.hijriMonth == currMonth) ev.hijriDay < currDay
        else false
    }

    val pastIndices = remember(currDay, currMonth) {
        ISLAMIC_HOLIDAYS.indices.filter { isPastPredicate(ISLAMIC_HOLIDAYS[it]) }.toSet()
    }

    val nextIndex = remember(currDay, currMonth, pastIndices) {
        ISLAMIC_HOLIDAYS.indices.firstOrNull { it !in pastIndices } ?: 0
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "رزنامة الأعياد والمناسبات الدينية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                val holidayCountStr = com.example.ui.util.AppLocalization.formatDigits(ISLAMIC_HOLIDAYS.size.toString(), numeralSystem)

                Text(
                    text = if (expanded) "طي ▲" else "عرض الكل ($holidayCountStr) ▼",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }

            // Preview Highlights or Full List
            val displayedIndices = if (expanded) {
                ISLAMIC_HOLIDAYS.indices.toList()
            } else {
                // Show next holiday + 2 upcoming/subsequent holidays
                val list = mutableListOf<Int>()
                list.add(nextIndex)
                for (offset in 1..2) {
                    val candidate = (nextIndex + offset) % ISLAMIC_HOLIDAYS.size
                    if (candidate !in list) list.add(candidate)
                }
                list
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayedIndices.forEach { index ->
                    val event = ISLAMIC_HOLIDAYS[index]
                    val status = when {
                        index == nextIndex -> IslamicHolidayStatus.NEXT
                        index in pastIndices -> IslamicHolidayStatus.PAST
                        else -> IslamicHolidayStatus.UPCOMING
                    }

                    val cardContainerColor = when (status) {
                        IslamicHolidayStatus.NEXT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        IslamicHolidayStatus.PAST -> Color(0xFFFFEBEE).copy(alpha = 0.6f)
                        IslamicHolidayStatus.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }

                    val borderColor = when (status) {
                        IslamicHolidayStatus.NEXT -> Color(0xFFD4AF37) // Gold Accent
                        IslamicHolidayStatus.PAST -> Color(0xFFE57373).copy(alpha = 0.6f)
                        IslamicHolidayStatus.UPCOMING -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    }

                    val titleColor = when (status) {
                        IslamicHolidayStatus.NEXT -> MaterialTheme.colorScheme.primary
                        IslamicHolidayStatus.PAST -> Color(0xFFD32F2F) // Red
                        IslamicHolidayStatus.UPCOMING -> Color(0xFF1A1A1A) // Black
                    }

                    val descColor = when (status) {
                        IslamicHolidayStatus.NEXT -> MaterialTheme.colorScheme.onSurface
                        IslamicHolidayStatus.PAST -> Color(0xFFC62828).copy(alpha = 0.85f)
                        IslamicHolidayStatus.UPCOMING -> Color(0xFF4A4A4A)
                    }

                    val dateBadgeBg = when (status) {
                        IslamicHolidayStatus.NEXT -> Color(0xFFD4AF37)
                        IslamicHolidayStatus.PAST -> Color(0xFFD32F2F).copy(alpha = 0.15f)
                        IslamicHolidayStatus.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val dateBadgeText = when (status) {
                        IslamicHolidayStatus.NEXT -> Color(0xFF1B3D2F)
                        IslamicHolidayStatus.PAST -> Color(0xFFD32F2F)
                        IslamicHolidayStatus.UPCOMING -> Color(0xFF1A1A1A)
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (status == IslamicHolidayStatus.NEXT) 1.5.dp else 1.dp,
                            color = borderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = event.iconEmoji,
                                    fontSize = 20.sp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = event.titleAr,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor
                                        )

                                        if (status == IslamicHolidayStatus.NEXT) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFD4AF37))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "المناسبة القادمة ⏳",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                            }
                                        } else if (status == IslamicHolidayStatus.PAST) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFD32F2F).copy(alpha = 0.15f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "مضت ✓",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD32F2F)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = event.descriptionAr,
                                        fontSize = 11.5.sp,
                                        color = descColor,
                                        maxLines = 1
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                                val rawHijriDate = "${event.hijriDay} ${event.hijriMonthNameAr}"
                                val formattedHijriDate = com.example.ui.util.AppLocalization.formatDigits(rawHijriDate, numeralSystem)
                                val formattedGregorian = com.example.ui.util.AppLocalization.formatDigits(event.gregorianDateAr, numeralSystem)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(dateBadgeBg)
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = formattedHijriDate,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dateBadgeText
                                    )
                                }
                                Text(
                                    text = formattedGregorian,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (status == IslamicHolidayStatus.PAST) Color(0xFFD32F2F).copy(alpha = 0.8f) else Color(0xFF666666),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PrayerItemData(val key: String, val nameAr: String, val time: String)

@Composable
private fun LocationHeaderCard(
    cityName: String,
    countryName: String,
    hijriDate: String?,
    onOpenLocationModal: () -> Unit,
    onOpenAdhanModal: () -> Unit,
    onOpenHijriModal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (cityName.isEmpty()) "مواقيت الصلاة" else "مواقيت - $cityName",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                if (!hijriDate.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(start = 21.dp, top = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenHijriModal)
                            .testTag("btn_adjust_hijri_date")
                    ) {
                        Text(
                            text = hijriDate ?: "",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تصحيح الهجري",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Adhan Sound Picker Pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onOpenAdhanModal)
                        .padding(7.dp)
                        .testTag("btn_change_adhan_sound")
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "صوت الأذان",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Location Selector Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        .clickable(onClick = onOpenLocationModal)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("btn_change_location")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (cityName.contains("الجزائر")) cityName else "$cityName، $countryName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class SimplePrayerData(val nameAr: String, val nameEn: String, val time: String)

private fun calculatePrayerProgressInfo(
    prayerTimings: PrayerTimings,
    nextPrayer: NextPrayerInfo
): Triple<SimplePrayerData?, SimplePrayerData?, Float> {
    val prayers = listOf(
        SimplePrayerData("الفجر", "Fajr", prayerTimings.fajr.take(5)),
        SimplePrayerData("الظهر", "Dhuhr", prayerTimings.dhuhr.take(5)),
        SimplePrayerData("العصر", "Asr", prayerTimings.asr.take(5)),
        SimplePrayerData("المغرب", "Maghrib", prayerTimings.maghrib.take(5)),
        SimplePrayerData("العشاء", "Isha", prayerTimings.isha.take(5))
    )

    val nextIdx = prayers.indexOfFirst { it.nameEn.equals(nextPrayer.nameEn, ignoreCase = true) }
    if (nextIdx == -1) {
        return Triple(null, null, 0f)
    }

    val prevIdx = if (nextIdx == 0) prayers.lastIndex else nextIdx - 1
    val prevPrayer = prayers[prevIdx]
    val nextItem = prayers[nextIdx]

    val now = Calendar.getInstance()
    fun parseToCal(timeStr: String, dayOffset: Int): Calendar {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: 0)
                set(Calendar.MINUTE, parts[1].toIntOrNull() ?: 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
        return cal
    }

    val (prevCal, nextCal) = if (nextPrayer.nameEn.equals("Fajr", ignoreCase = true)) {
        if (now.get(Calendar.HOUR_OF_DAY) >= 12) {
            parseToCal(prevPrayer.time, 0) to parseToCal(nextItem.time, 1)
        } else {
            parseToCal(prevPrayer.time, -1) to parseToCal(nextItem.time, 0)
        }
    } else {
        parseToCal(prevPrayer.time, 0) to parseToCal(nextItem.time, 0)
    }

    val totalSpan = (nextCal.timeInMillis - prevCal.timeInMillis).toFloat()
    val elapsed = (now.timeInMillis - prevCal.timeInMillis).toFloat()
    val ratio = if (totalSpan > 0f) (elapsed / totalSpan).coerceIn(0f, 1f) else 0f

    return Triple(prevPrayer, nextItem, ratio)
}

@Composable
private fun CountdownHeroCard(
    nextPrayer: NextPrayerInfo?,
    prayerTimings: PrayerTimings?,
    countdownText: String,
    currentTimeText: String = "",
    isLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            if (nextPrayer != null && prayerTimings != null) {
                val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val (prevPrayer, _, progressRatio) = remember(nextPrayer, prayerTimings) {
                    calculatePrayerProgressInfo(prayerTimings, nextPrayer)
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = progressRatio,
                    animationSpec = tween(durationMillis = 600),
                    label = "hero_prayer_progress"
                )

                val formattedCountdown = com.example.ui.util.AppLocalization.formatDigits(countdownText, numeralSystem)
                val nextPrayerFormattedTime = com.example.ui.util.AppLocalization.formatDigits(nextPrayer.formattedTime, numeralSystem)
                val prevPrayerFormattedTime = prevPrayer?.let {
                    com.example.ui.util.AppLocalization.formatDigits(it.time, numeralSystem)
                } ?: ""

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. التسمية العلوية: "الأذان بعد"
                    Text(
                        text = if (isRtl) "الأذان بعد" else "Adhan in",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )

                    // 2. العد التنازلي بحجم كبير وواضح
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = formattedCountdown,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimary,
                            letterSpacing = 2.sp
                        )
                    }

                    // 3. اسم الصلاة القادمة ووقتها في سطر واحد
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRtl) nextPrayer.nameAr else nextPrayer.nameEn,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = nextPrayerFormattedTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 4. شريط التقدم بين الصلاة السابقة والقادمة
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress.coerceIn(0.01f, 1f))
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }

                    // 5. السطر السفلي: الصلاة السابقة والصلاة القادمة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (prevPrayer != null) {
                            Text(
                                text = "${if (isRtl) prevPrayer.nameAr else prevPrayer.nameEn} $prevPrayerFormattedTime",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Text(
                            text = "${if (isRtl) nextPrayer.nameAr else nextPrayer.nameEn} $nextPrayerFormattedTime",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جاري حساب مواقيت الصلاة القادمة...",
                        fontSize = 14.sp,
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerCard(
    title: String,
    time: String,
    isNext: Boolean,
    is24HourFormat: Boolean = true,
    appLanguage: String = "ar"
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .border(
                width = if (isNext) 2.dp else 1.dp,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("prayer_card_${title}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoSizingText(
                text = title,
                targetFontSize = 14.5.sp,
                minFontSize = 11.sp,
                fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )

            val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
            val formattedTime = com.example.ui.util.AppLocalization.formatTime(time, is24HourFormat, appLanguage, numeralSystem)
            AutoSizingText(
                text = formattedTime,
                targetFontSize = 16.5.sp,
                minFontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontFeatureSettings = "tnum",
                lineHeightMultiplier = 1.35f,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
