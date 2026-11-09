package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerAdhanSettings
import com.example.data.model.PrayerTimings
import com.example.ui.components.AutoSizingAnnotatedText
import com.example.ui.components.AutoSizingText
import com.example.ui.theme.AppThemeTokens
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.LocalAppThemeTokens
import com.example.ui.util.AppLocalization
import com.example.util.MosqueBackgroundHelper
import java.util.Locale

private data class PrayerTileEntry(
    val key: String,
    val nameAr: String,
    val time: String,
    val isNext: Boolean,
    val soundEnabled: Boolean,
    val isSunrise: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerClockScreen(
    cityName: String,
    currentTimeText: String = "",
    prayerTimings: PrayerTimings?,
    nextPrayer: NextPrayerInfo?,
    prayerAdhanSettings: PrayerAdhanSettings = PrayerAdhanSettings(),
    useRoosterForSunrise: Boolean = true,
    onToggleRoosterForSunrise: (Boolean) -> Unit = {},
    onTogglePrayerAdhan: (String, Boolean) -> Unit = { _, _ -> },
    isDarkMode: Boolean = false,
    backgroundThemeId: String = "emerald",
    showMosqueBackgrounds: Boolean = true,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = onOpenDrawer,
    onOpenLocationDialog: () -> Unit = {},
    onOpenNearestMosque: () -> Unit = {},
    onCycleTheme: () -> Unit = {},
    appLanguage: String = "ar",
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppThemeTokens.current

    val fivePrayers = remember(prayerTimings, prayerAdhanSettings, appLanguage) {
        listOf(
            PrayerTileEntry("fajr", AppLocalization.getPrayerName("fajr", appLanguage), prayerTimings?.fajr?.take(5) ?: "05:12", false, prayerAdhanSettings.fajr),
            PrayerTileEntry("dhuhr", AppLocalization.getPrayerName("dhuhr", appLanguage), prayerTimings?.dhuhr?.take(5) ?: "12:35", false, prayerAdhanSettings.dhuhr),
            PrayerTileEntry("asr", AppLocalization.getPrayerName("asr", appLanguage), prayerTimings?.asr?.take(5) ?: "15:58", false, prayerAdhanSettings.asr),
            PrayerTileEntry("maghrib", AppLocalization.getPrayerName("maghrib", appLanguage), prayerTimings?.maghrib?.take(5) ?: "18:32", false, prayerAdhanSettings.maghrib),
            PrayerTileEntry("isha", AppLocalization.getPrayerName("isha", appLanguage), prayerTimings?.isha?.take(5) ?: "19:48", false, prayerAdhanSettings.isha)
        )
    }

    val nextPrayerName = if (appLanguage == "en") (nextPrayer?.nameEn ?: "Fajr") else (nextPrayer?.nameAr ?: "الفجر")
    val nextFiveIndex = remember(nextPrayerName, fivePrayers) {
        val found = fivePrayers.indexOfFirst { it.nameAr.equals(nextPrayerName, ignoreCase = true) }
        if (found != -1) found else 0
    }
    val prevFiveIndex = if (nextFiveIndex == 0) fivePrayers.size - 1 else nextFiveIndex - 1
    val prevPrayer = fivePrayers[prevFiveIndex]
    val nextPrayerItem = fivePrayers[nextFiveIndex]

    val remainingMillis = (nextPrayer?.remainingMillis ?: 0L).coerceAtLeast(0L)
    val rawProgress = remember(remainingMillis, prevPrayer.time, nextPrayerItem.time) {
        computePrayerProgress(prevPrayer.time, nextPrayerItem.time, remainingMillis)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "countdown_progress_bar"
    )

    val hours = remainingMillis / 3600000
    val minutes = (remainingMillis % 3600000) / 60000
    val seconds = (remainingMillis % 60000) / 1000

    val gridPrayers = remember(prayerTimings, nextPrayer, prayerAdhanSettings, useRoosterForSunrise, appLanguage) {
        val nextKey = when (nextPrayer?.nameEn?.lowercase() ?: nextPrayer?.nameAr) {
            "fajr", "الفجر" -> "fajr"
            "sunrise", "الشروق" -> "sunrise"
            "dhuhr", "الظهر" -> "dhuhr"
            "asr", "العصر" -> "asr"
            "maghrib", "المغرب" -> "maghrib"
            "isha", "العشاء" -> "isha"
            else -> "fajr"
        }
        listOf(
            PrayerTileEntry("fajr", AppLocalization.getPrayerName("fajr", appLanguage), prayerTimings?.fajr?.take(5) ?: "05:12", nextKey == "fajr", prayerAdhanSettings.fajr),
            PrayerTileEntry("sunrise", AppLocalization.getPrayerName("sunrise", appLanguage), prayerTimings?.sunrise?.take(5) ?: "06:34", nextKey == "sunrise", useRoosterForSunrise, isSunrise = true),
            PrayerTileEntry("dhuhr", AppLocalization.getPrayerName("dhuhr", appLanguage), prayerTimings?.dhuhr?.take(5) ?: "12:35", nextKey == "dhuhr", prayerAdhanSettings.dhuhr),
            PrayerTileEntry("asr", AppLocalization.getPrayerName("asr", appLanguage), prayerTimings?.asr?.take(5) ?: "15:58", nextKey == "asr", prayerAdhanSettings.asr),
            PrayerTileEntry("maghrib", AppLocalization.getPrayerName("maghrib", appLanguage), prayerTimings?.maghrib?.take(5) ?: "18:32", nextKey == "maghrib", prayerAdhanSettings.maghrib),
            PrayerTileEntry("isha", AppLocalization.getPrayerName("isha", appLanguage), prayerTimings?.isha?.take(5) ?: "19:48", nextKey == "isha", prayerAdhanSettings.isha)
        )
    }

    val systemDensity = LocalDensity.current
    // Requirement 7: Cap fontScale at 1.4 for critical components (countdown, prayer tiles)
    val cappedDensity = remember(systemDensity) {
        Density(
            density = systemDensity.density,
            fontScale = systemDensity.fontScale.coerceAtMost(1.4f)
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (showMosqueBackgrounds) Color.Transparent else tokens.bg),
                contentAlignment = Alignment.TopCenter
            ) {
                if (showMosqueBackgrounds) {
                    val activePrayer = nextPrayer?.nameEn ?: nextPrayer?.nameAr ?: "dhuhr"
                    val mosqueInfo = remember(activePrayer) { MosqueBackgroundHelper.getMosqueForPrayer(activePrayer) }
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
                val availableWidthDp = maxWidth
                // Requirement 5: When fontScale > 1.3 or available width < 340dp, switch to single column
                val isSingleColumn = systemDensity.fontScale > 1.3f || availableWidthDp < 340.dp

                val scrollState = rememberScrollState()
                val mainModifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)

                Column(
                    modifier = mainModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1) الترويسة: أيقونة الإعدادات في اليمين (ثابتة مع باقي الصفحات) واسم المدينة في اليسار
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(onClick = onOpenSettings)
                                .testTag("header_settings_icon"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings_tasbih),
                                contentDescription = if (appLanguage == "en") "Settings" else "الإعدادات",
                                tint = if (isDarkMode) Color(0xFFFFD60A) else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        AutoSizingText(
                            text = cityName.ifEmpty { "ورقلة" },
                            targetFontSize = 28.sp,
                            minFontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFontFamily,
                            color = tokens.ink,
                            lineHeightMultiplier = 1.35f,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable(onClick = onOpenLocationDialog)
                                .testTag("header_city_name")
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 2) بطاقة الصلاة القادمة والعداد التنازلي: ارتفاع مرن يتبع المحتوى مع حد أدنى
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 115.dp)
                            .testTag("next_prayer_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // سطر واحد: اسم الصلاة على اليمين والعدّاد LTR على اليسار
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AutoSizingText(
                                    text = nextPrayerName,
                                    targetFontSize = 40.sp,
                                    minFontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily,
                                    color = tokens.accent,
                                    lineHeightMultiplier = 1.3f,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // العد التنازلي بأرقام ثابتة العرض (tnum) وتصغير تلقائي لمنع الانضغاط أو الاهتزاز
                                CompositionLocalProvider(
                                    LocalDensity provides cappedDensity,
                                    LocalLayoutDirection provides LayoutDirection.Ltr
                                ) {
                                    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                                    val countdownAnnotated = remember(hours, minutes, seconds, numeralSystem, tokens.colon, tokens.accent) {
                                        val h = AppLocalization.formatDigits(String.format(Locale.US, "%02d", hours), numeralSystem)
                                        val m = AppLocalization.formatDigits(String.format(Locale.US, "%02d", minutes), numeralSystem)
                                        val s = AppLocalization.formatDigits(String.format(Locale.US, "%02d", seconds), numeralSystem)
                                        buildAnnotatedString {
                                            withStyle(SpanStyle(color = Color.White)) { append(h) }
                                            withStyle(SpanStyle(color = tokens.colon)) { append(":") }
                                            withStyle(SpanStyle(color = Color.White)) { append(m) }
                                            withStyle(SpanStyle(color = tokens.colon)) { append(":") }
                                            withStyle(SpanStyle(color = tokens.accent)) { append(s) }
                                        }
                                    }

                                    AutoSizingAnnotatedText(
                                        text = countdownAnnotated,
                                        targetFontSize = 34.sp,
                                        minFontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        fontFeatureSettings = "tnum",
                                        lineHeightMultiplier = 1.3f,
                                        maxLines = 1
                                    )
                                }
                            }

                            // شريط التقدم بسماكة 12dp
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                            ) {
                                drawRoundRect(
                                    color = tokens.colon.copy(alpha = 0.35f),
                                    size = size,
                                    cornerRadius = CornerRadius(size.height / 2f)
                                )
                                val progressWidth = size.width * animatedProgress.coerceIn(0f, 1f)
                                drawRoundRect(
                                    color = tokens.accent,
                                    topLeft = Offset(size.width - progressWidth, 0f),
                                    size = Size(progressWidth, size.height),
                                    cornerRadius = CornerRadius(size.height / 2f)
                                )
                            }

                            // سطر لوقتي الصلاة السابقة والقادمة مع منع القص
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                                Text(
                                    text = "${prevPrayer.nameAr} ${AppLocalization.formatTime(prevPrayer.time, prayerAdhanSettings.is24HourFormat, appLanguage, numeralSystem)}",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = CairoFontFamily,
                                    color = tokens.cardSub,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.None
                                        )
                                    )
                                )
                                Text(
                                    text = "${nextPrayerItem.nameAr} ${AppLocalization.formatTime(nextPrayerItem.time, prayerAdhanSettings.is24HourFormat, appLanguage, numeralSystem)}",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = CairoFontFamily,
                                    color = tokens.cardSub,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.None
                                        )
                                    )
                                )
                            }
                        }
                    }

                    // 3) شبكة المواقيت: مرنة وتتكيف مع حجم الخط وعرض الشاشة
                    CompositionLocalProvider(LocalDensity provides cappedDensity) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSingleColumn) {
                                gridPrayers.forEach { entry ->
                                    PrayerTile(
                                        entry = entry,
                                        tokens = tokens,
                                        appLanguage = appLanguage,
                                        onToggleSound = {
                                            com.example.util.ClickSoundPlayer.playClick()
                                            if (entry.isSunrise) {
                                                onToggleRoosterForSunrise(!entry.soundEnabled)
                                            } else {
                                                onTogglePrayerAdhan(entry.key, !entry.soundEnabled)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 70.dp)
                                    )
                                }
                            } else {
                                gridPrayers.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 85.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { entry ->
                                            PrayerTile(
                                                entry = entry,
                                                tokens = tokens,
                                                appLanguage = appLanguage,
                                                is24HourFormat = prayerAdhanSettings.is24HourFormat,
                                                onToggleSound = {
                                                    com.example.util.ClickSoundPlayer.playClick()
                                                    if (entry.isSunrise) {
                                                        onToggleRoosterForSunrise(!entry.soundEnabled)
                                                    } else {
                                                        onTogglePrayerAdhan(entry.key, !entry.soundEnabled)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 85.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // منطقة تفاعلية سفلية لتبديل الثيم
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 28.dp, max = 56.dp)
                            .padding(vertical = 4.dp)
                            .testTag("tap_area_cycle_theme")
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onCycleTheme()
                            }
                    )



                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PrayerTile(
    entry: PrayerTileEntry,
    tokens: AppThemeTokens,
    appLanguage: String,
    is24HourFormat: Boolean = true,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileBg = if (entry.isNext) tokens.nextBg else tokens.tile
    val tileBorderColor = if (entry.isNext) tokens.nextBg else tokens.tileBorder
    val nameColor = if (entry.isNext) tokens.nextName else tokens.tileName
    val timeColor = if (entry.isNext) tokens.nextTime else tokens.tileTime
    val localizedName = AppLocalization.getPrayerName(entry.key, appLanguage)
    val speakerColor = if (entry.soundEnabled) Color(0xFFF59E0B) else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tileBg)
            .border(1.5.dp, tileBorderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("prayer_tile_${entry.key}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                AutoSizingText(
                    text = localizedName,
                    targetFontSize = 19.sp,
                    minFontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    color = nameColor,
                    textAlign = TextAlign.Center,
                    lineHeightMultiplier = 1.35f,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_toggle_sound_${entry.key}")
                ) {
                    if (entry.isSunrise) {
                        Icon(
                            imageVector = if (entry.soundEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = if (entry.soundEnabled) "إيقاف تنبيه الشروق" else "تشغيل تنبيه الشروق",
                            tint = if (entry.soundEnabled) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (entry.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = if (entry.soundEnabled) "كتم الأذان" else "تشغيل الأذان",
                            tint = speakerColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
            val formattedTime = AppLocalization.formatTime(entry.time, is24HourFormat, appLanguage, numeralSystem)
            AutoSizingText(
                text = formattedTime,
                targetFontSize = 32.sp,
                minFontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = CairoFontFamily,
                color = timeColor,
                textAlign = TextAlign.Center,
                fontFeatureSettings = "tnum",
                lineHeightMultiplier = 1.35f,
                maxLines = 1
            )
        }
    }
}

private fun computePrayerProgress(
    prevPrayerTime: String,
    nextPrayerTime: String,
    remainingMillis: Long
): Float {
    return try {
        val prevParts = prevPrayerTime.split(":")
        val prevHour = prevParts[0].trim().toInt()
        val prevMin = prevParts[1].trim().toInt()

        val nextParts = nextPrayerTime.split(":")
        val nextHour = nextParts[0].trim().toInt()
        val nextMin = nextParts[1].trim().toInt()

        var prevTotalMin = prevHour * 60 + prevMin
        var nextTotalMin = nextHour * 60 + nextMin

        if (nextTotalMin <= prevTotalMin) {
            nextTotalMin += 24 * 60
        }

        val totalIntervalMinutes = (nextTotalMin - prevTotalMin).coerceAtLeast(1)
        val remainingMinutes = (remainingMillis / 60000.0).coerceAtLeast(0.0)
        val elapsedMinutes = (totalIntervalMinutes - remainingMinutes).coerceAtLeast(0.0)

        (elapsedMinutes / totalIntervalMinutes).toFloat().coerceIn(0f, 1f)
    } catch (_: Exception) {
        0.5f
    }
}
