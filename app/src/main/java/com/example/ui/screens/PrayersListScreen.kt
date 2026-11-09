package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.util.MosqueBackgroundHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PRESET_BACKGROUND_THEMES
import com.example.data.model.PrayerAdhanSettings
import com.example.data.model.PrayerTimings
import com.example.ui.util.AppLocalization
import androidx.compose.material.icons.filled.LocationOn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersListScreen(
    cityName: String,
    hijriDate: String?,
    prayerTimings: PrayerTimings?,
    nextPrayer: NextPrayerInfo?,
    prayerAdhanSettings: PrayerAdhanSettings,
    onTogglePrayerAdhan: (String, Boolean) -> Unit,
    useRoosterForSunrise: Boolean = true,
    onToggleRoosterForSunrise: (Boolean) -> Unit = {},
    isDarkMode: Boolean,
    backgroundThemeId: String = "parchment",
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = onOpenDrawer,
    onOpenLocationDialog: () -> Unit,
    onOpenNearestMosque: () -> Unit = {},
    onCycleBackgroundTheme: () -> Unit = {},
    appLanguage: String = "ar",
    latitude: Double = 31.9593,
    longitude: Double = 5.3074,
    calculationMethodId: Int = 16,
    prayerOffsets: com.example.data.model.PrayerOffsets = com.example.data.model.PrayerOffsets(),
    hijriOffset: Int = 0,
    asrMethod: String = "standard",
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    showMosqueBackgrounds: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isAr = appLanguage != "en"
    val theme = PRESET_BACKGROUND_THEMES.find { it.id == backgroundThemeId } ?: PRESET_BACKGROUND_THEMES.first()
    val backgroundColor = if (isDarkMode) Color(theme.darkBgColor) else Color(theme.lightBgColor)
    var dayOffset by remember { mutableIntStateOf(0) }

    // Calculate display date
    val calendar = remember(dayOffset) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }
    }

    val dayOfWeekName = remember(calendar, appLanguage) {
        val sdf = SimpleDateFormat("EEEE", Locale(if (isAr) "ar" else "en"))
        sdf.format(calendar.time)
    }

    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current

    val gregorianDateStr = remember(calendar, appLanguage, numeralSystem) {
        val sdf = SimpleDateFormat(if (isAr) "d MMMM yyyy" else "MMMM d, yyyy", Locale(if (isAr) "ar" else "en"))
        sdf.format(calendar.time).toConfiguredDigits(numeralSystem)
    }

    // Dynamic prayer timings for current dayOffset
    val effectiveTimings = remember(dayOffset, prayerTimings, latitude, longitude, calculationMethodId, prayerOffsets, asrMethod) {
        if (dayOffset == 0 && prayerTimings != null) {
            prayerTimings
        } else {
            com.example.data.calculator.AdhanCalculator.calculatePrayerTimes(
                latitude = latitude,
                longitude = longitude,
                date = calendar.time,
                methodId = calculationMethodId,
                offsets = prayerOffsets,
                asrMethod = asrMethod
            )
        }
    }

    // Dynamic Hijri date for current dayOffset
    val effectiveHijriDate = remember(dayOffset, hijriDate, calendar, hijriOffset, appLanguage, numeralSystem) {
        if (dayOffset == 0 && !hijriDate.isNullOrBlank()) {
            val localized = AppLocalization.getLocalizedHijriDate(hijriDate, appLanguage)
            if (isAr) localized.toConfiguredDigits(numeralSystem) else localized
        } else {
            try {
                val localDate = java.time.Instant.ofEpochMilli(calendar.timeInMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                val baseHijri = java.time.chrono.HijrahChronology.INSTANCE.date(localDate)
                val adjustedHijri = if (hijriOffset != 0) {
                    baseHijri.plus(hijriOffset.toLong(), java.time.temporal.ChronoUnit.DAYS)
                } else {
                    baseHijri
                }
                val day = adjustedHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                val month = adjustedHijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
                val year = adjustedHijri.get(java.time.temporal.ChronoField.YEAR)
                val formatted = AppLocalization.formatHijriDate(day, month, year, appLanguage)
                if (isAr) formatted.toConfiguredDigits(numeralSystem) else formatted
            } catch (_: Exception) {
                val localized = AppLocalization.getLocalizedHijriDate(hijriDate, appLanguage)
                if (isAr) localized.toConfiguredDigits(numeralSystem) else localized
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        val showMosqueBg = showMosqueBackgrounds
        val activePrayer = nextPrayer?.nameEn ?: nextPrayer?.nameAr ?: "dhuhr"
        val mosqueInfo = remember(activePrayer) { MosqueBackgroundHelper.getMosqueForPrayer(activePrayer) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (showMosqueBg) Color.Transparent else backgroundColor)
        ) {
            if (showMosqueBg) {
                Image(
                    painter = painterResource(id = mosqueInfo.drawableResId),
                    contentDescription = mosqueInfo.nameAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Menu Button at Start, Centered City Name, Balanced Spacer at End
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable(onClick = onOpenSettings)
                            .testTag("btn_open_settings_list"),
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
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenLocationDialog() }
                            .padding(vertical = 4.dp)
                            .testTag("btn_city_header_list")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = AppLocalization.getLocalizedCityName(cityName, appLanguage),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (showMosqueBg) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = if (isAr) "الموقع مثبت" else "Pinned Location",
                                tint = if (showMosqueBg) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = AppLocalization.getString("fixed_location", appLanguage),
                            fontSize = 11.sp,
                            color = if (showMosqueBg) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.size(44.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date Header with Day Browsing Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isAr) {
                        // Date Details on Far Right for Arabic
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dayOfWeekName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showMosqueBg) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Hijri Date in Cyan
                            Text(
                                text = effectiveHijriDate.ifBlank { if (isAr) "جاري الحساب..." else "Calculating..." },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00ACC1),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                softWrap = false
                            )

                            // Gregorian Date
                            Text(
                                text = gregorianDateStr,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Day browsing arrows on Left
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { dayOffset++ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "اليوم التالي",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { dayOffset-- },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "اليوم السابق",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // English Order (LTR)
                        IconButton(
                            onClick = { dayOffset++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = dayOfWeekName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Text(
                                text = effectiveHijriDate.ifBlank { if (isAr) "جاري الحساب..." else "Calculating..." },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00ACC1),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                softWrap = false
                            )

                            Text(
                                text = gregorianDateStr,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        IconButton(
                            onClick = { dayOffset-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // 6 Prayers Vertical List (Matching Screenshot 2)
                if (effectiveTimings != null) {
                    val prayerItems = listOf(
                        PrayerRowModel("fajr", if (isAr) "الفجر" else "Fajr", effectiveTimings.fajr, prayerAdhanSettings.fajr),
                        PrayerRowModel("sunrise", if (isAr) "الشروق" else "Sunrise", effectiveTimings.sunrise, useRoosterForSunrise, isSunrise = true),
                        PrayerRowModel("dhuhr", if (isAr) "الظهر" else "Dhuhr", effectiveTimings.dhuhr, prayerAdhanSettings.dhuhr),
                        PrayerRowModel("asr", if (isAr) "العصر" else "Asr", effectiveTimings.asr, prayerAdhanSettings.asr),
                        PrayerRowModel("maghrib", if (isAr) "المغرب" else "Maghrib", effectiveTimings.maghrib, prayerAdhanSettings.maghrib),
                        PrayerRowModel("isha", if (isAr) "العشاء" else "Isha", effectiveTimings.isha, prayerAdhanSettings.isha)
                    )

                    // Remaining countdown string (Hours:Minutes:Seconds)
                    val remainingMillis = (nextPrayer?.remainingMillis ?: 0L).coerceAtLeast(0)
                    val h = remainingMillis / 3600000
                    val m = (remainingMillis % 3600000) / 60000
                    val s = (remainingMillis % 60000) / 1000
                    val countdownFormatted = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        prayerItems.forEach { item ->
                            val isNext = (nextPrayer?.nameAr == item.nameAr || nextPrayer?.nameEn == item.nameAr) && dayOffset == 0

                            PrayerListRow(
                                item = item,
                                isNext = isNext,
                                countdownText = if (isNext) countdownFormatted else null,
                                appLanguage = appLanguage,
                                isDarkSurface = (isDarkMode || showMosqueBg),
                                is24HourFormat = prayerAdhanSettings.is24HourFormat,
                                onToggleSound = {
                                    com.example.util.ClickSoundPlayer.playClick()
                                    if (item.isSunrise) {
                                        onToggleRoosterForSunrise(!item.soundEnabled)
                                    } else {
                                        onTogglePrayerAdhan(item.key, !item.soundEnabled)
                                    }
                                }
                            )
                        }
                    }
                }



                // Empty Interactive Tap Area: Click anywhere in this space below prayer timings to cycle theme colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("tap_area_cycle_theme_list")
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            onCycleBackgroundTheme()
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}


data class PrayerRowModel(
    val key: String,
    val nameAr: String,
    val time: String,
    val soundEnabled: Boolean,
    val isSunrise: Boolean = false
)

@Composable
fun PrayerListRow(
    item: PrayerRowModel,
    isNext: Boolean,
    countdownText: String?,
    appLanguage: String,
    isDarkSurface: Boolean = false,
    is24HourFormat: Boolean = true,
    onToggleSound: () -> Unit
) {
    val speakerColor = if (item.soundEnabled) Color(0xFFF59E0B) else Color(0xFF94A3B8)
    val localizedName = AppLocalization.getPrayerName(item.key, appLanguage)
    val isAr = appLanguage != "en"
    val textColor = if (isDarkSurface) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isNext) (if (isDarkSurface) Color(0x334ADE80) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAr) {
            // Arabic (RTL exact sequence): Prayer Name -> Countdown -> Time -> Speaker Icon
            Text(
                text = localizedName,
                fontSize = if (isNext) 21.sp else 19.sp,
                fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isNext && isDarkSurface) Color(0xFF4ADE80) else textColor,
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false
            )

            if (countdownText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = countdownText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
            Text(
                text = AppLocalization.formatTime(item.time, is24HourFormat, appLanguage, numeralSystem),
                fontSize = if (isNext) 22.sp else 20.sp,
                fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isNext && isDarkSurface) Color(0xFF4ADE80) else textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 1,
                softWrap = false
            )

            IconButton(
                onClick = onToggleSound,
                enabled = true,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_toggle_sound_${item.key}")
            ) {
                if (item.isSunrise) {
                    if (item.soundEnabled) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "صوت الديك للشروق مفعل",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "صوت الديك للشروق مكتوم",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else if (item.soundEnabled) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "الأذان مفعل",
                        tint = speakerColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = "الأذان مكتوم",
                        tint = speakerColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // English (LTR): Sound on Left, Name in middle, Time on Right
            IconButton(
                onClick = onToggleSound,
                enabled = true,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_toggle_sound_${item.key}")
            ) {
                if (item.isSunrise) {
                    if (item.soundEnabled) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Rooster sound enabled",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Rooster sound muted",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else if (item.soundEnabled) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Adhan enabled",
                        tint = speakerColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = "Adhan muted",
                        tint = speakerColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                if (countdownText != null) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = countdownText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = if (isDarkSurface) Color(0xFF4ADE80) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = localizedName,
                    fontSize = if (isNext) 21.sp else 19.sp,
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isNext && isDarkSurface) Color(0xFF4ADE80) else textColor,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    softWrap = false
                )
            }

            val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
            Text(
                text = AppLocalization.formatTime(item.time, is24HourFormat, appLanguage, numeralSystem),
                fontSize = if (isNext) 22.sp else 20.sp,
                fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isNext && isDarkSurface) Color(0xFF4ADE80) else textColor,
                modifier = Modifier.width(68.dp),
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private fun String.toConfiguredDigits(numeralSystem: String): String {
    return com.example.ui.util.AppLocalization.formatDigits(this, numeralSystem)
}
