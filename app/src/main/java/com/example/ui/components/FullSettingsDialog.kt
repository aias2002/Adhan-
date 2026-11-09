package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.BuildConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import com.example.util.PrayerAutoSilentManager
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.COMMON_CALCULATION_METHODS
import com.example.data.model.PRESET_ADHAN_SOUNDS
import com.example.data.model.PrayerAdhanSettings
import com.example.data.model.PrayerOffsets
import com.example.ui.theme.CairoFontFamily
import com.example.util.AdhanAudioPlayer

// Model for structured menu section and items
data class SettingRowData(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: (() -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null
)

data class SettingSectionData(
    val id: String,
    val title: String,
    val rows: List<SettingRowData>
)

// Helper components for structured menu
@Composable
fun SettingsSectionNew(
    sectionTitle: String,
    rows: List<SettingRowData>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isAr: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) {
                    if (isAr) "طي القسم" else "Collapse Section"
                } else {
                    if (isAr) "توسيع القسم" else "Expand Section"
                },
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    rows.forEachIndexed { index, row ->
                        SettingRow(row = row, isAr = isAr)
                        if (index < rows.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingRow(row: SettingRowData, isAr: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(enabled = row.onClick != null, onClick = { row.onClick?.invoke() })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (row.subtitle.isNotEmpty()) {
                Text(
                    text = row.subtitle,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (row.trailingContent != null) {
            row.trailingContent.invoke()
        } else if (row.onClick != null) {
            Icon(
                imageVector = if (isAr) Icons.Default.ChevronLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FullSettingsDialog(
    // App Language & Numeral System
    appLanguage: String = "ar",
    onSelectLanguage: (String) -> Unit = {},
    numeralSystem: String = "western",
    onSelectNumeralSystem: (String) -> Unit = {},
    // Notification Style
    notificationStyle: String = "POPUP",
    onSelectNotificationStyle: (String) -> Unit = {},
    // Calculation Method & Asr School Setting
    calculationMethodId: Int,
    onSelectCalculationMethod: (Int) -> Unit,
    asrMethod: String = "standard",
    onSelectAsrMethod: (String) -> Unit = {},
    // Fajr Adhan Formula
    fajrFormulaWithTatweeb: Boolean = true,
    onToggleFajrFormulaWithTatweeb: (Boolean) -> Unit = {},
    // Prayer Adhan Settings
    prayerAdhanSettings: PrayerAdhanSettings,
    onTogglePrayerAdhan: (String, Boolean) -> Unit,
    onToggle24HourFormat: (Boolean) -> Unit = {},
    // Prayer Offsets
    prayerOffsets: PrayerOffsets = PrayerOffsets(),
    onUpdateOffsets: (PrayerOffsets) -> Unit = {},
    // Adhan Sound Selection
    selectedSoundId: String,
    customSoundUri: String?,
    customSoundName: String?,
    onSaveAdhanSound: (soundId: String, customUri: String?, customName: String?) -> Unit,
    // Location
    cityName: String,
    countryName: String,
    onOpenLocationDialog: () -> Unit,
    onFindNearestMosque: () -> Unit = {},
    onOpenPrayerOffsets: () -> Unit = {},
    onOpenContactUs: () -> Unit = {},
    // Hijri Adjustment
    hijriDate: String?,
    hijriOffset: Int,
    onUpdateHijriOffset: (Int) -> Unit,
    // Appearance & Sound
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    appThemeStyle: String = "emerald",
    onSelectThemeStyle: (String) -> Unit = {},
    followSystemDarkMode: Boolean = true,
    onToggleFollowSystemDarkMode: (Boolean) -> Unit = {},
    appThemePalette: String,
    onSelectPalette: (String) -> Unit,
    appBackgroundTheme: String = "parchment",
    onSelectBackgroundTheme: (String) -> Unit = {},
    isMuted: Boolean = false,
    onToggleMute: () -> Unit = {},
    overrideSilentMode: Boolean = true,
    onToggleOverrideSilentMode: (Boolean) -> Unit = {},
    useFlipToMute: Boolean = true,
    onToggleFlipToMute: (Boolean) -> Unit = {},
    adhanAudioStreamType: String = "media",
    onSetAdhanAudioStreamType: (String) -> Unit = {},
    useSdCardForDownloads: Boolean = false,
    onSetUseSdCardForDownloads: (Boolean) -> Unit = {},
    isDownloadingAllTexts: Boolean = false,
    downloadProgress: Float = 0f,
    downloadStatus: String? = null,
    onDownloadAllTexts: () -> Unit = {},
    selectedReciterId: String = "ar.alijaber",
    downloadingSurahNumber: Int? = null,
    onDownloadAllAudio: () -> Unit = {},
    // Lock Screen & Notification shade
    showLockScreenPrayerNotification: Boolean = true,
    onToggleLockScreenPrayerNotification: (Boolean) -> Unit = {},
    // Mosque backgrounds
    showMosqueBackgrounds: Boolean = true,
    onToggleMosqueBackgrounds: (Boolean) -> Unit = {},
    // Dismiss
    onDismiss: () -> Unit
) {
    val isAr = appLanguage != "en"
    val context = LocalContext.current
    val audioPlayer = remember { AdhanAudioPlayer(context) }
    var playingUrl by remember { mutableStateOf<String?>(null) }

    var currentSoundId by remember { mutableStateOf(selectedSoundId) }
    var activeCustomUri by remember { mutableStateOf(customSoundUri) }
    var activeCustomName by remember { mutableStateOf(customSoundName) }

    // Sub-dialog navigation state
    var activeSubDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedSections by rememberSaveable { mutableStateOf(setOf<String>()) }

    val silentPrefs = remember { PrayerAutoSilentManager.getPrefs(context) }
    var autoSilentEnabled by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_ENABLED, false)) }
    var autoSilentModeType by remember { mutableStateOf(silentPrefs.getString(PrayerAutoSilentManager.KEY_MODE_TYPE, "silent") ?: "silent") }
    var autoSilentDelay by remember { mutableStateOf(silentPrefs.getInt(PrayerAutoSilentManager.KEY_DELAY_MINUTES, 0)) }
    var autoSilentDuration by remember { mutableStateOf(silentPrefs.getInt(PrayerAutoSilentManager.KEY_DURATION_MINUTES, 20)) }
    var transitionAlertEnabled by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_TRANSITION_ALERT, true)) }
    var silentFajr by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_FAJR, true)) }
    var silentDhuhr by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_DHUHR, true)) }
    var silentAsr by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_ASR, true)) }
    var silentMaghrib by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_MAGHRIB, true)) }
    var silentIsha by remember { mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_ISHA, true)) }
    var hasPolicyAccess by remember { mutableStateOf(PrayerAutoSilentManager.hasNotificationPolicyAccess(context)) }

    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdatesState by remember { mutableStateOf(false) }
    var manualUpdateDialogInfo by remember { mutableStateOf<com.example.util.AppUpdateManifest?>(null) }
    val installedVersionCode = remember { com.example.util.AppUpdateManager.getInstalledVersionCode(context) }
    val installedVersionName = remember { com.example.util.AppUpdateManager.getInstalledVersionName(context) }

    LaunchedEffect(Unit) {
        com.example.util.AppUpdateManager.performCleanupIfUpToDate(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stopSound()
        }
    }

    LaunchedEffect(activeSubDialog) {
        if (activeSubDialog == "auto_silent") {
            hasPolicyAccess = PrayerAutoSilentManager.hasNotificationPolicyAccess(context)
            autoSilentEnabled = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_ENABLED, false)
            autoSilentModeType = silentPrefs.getString(PrayerAutoSilentManager.KEY_MODE_TYPE, "silent") ?: "silent"
            autoSilentDelay = silentPrefs.getInt(PrayerAutoSilentManager.KEY_DELAY_MINUTES, 0)
            autoSilentDuration = silentPrefs.getInt(PrayerAutoSilentManager.KEY_DURATION_MINUTES, 20)
            silentFajr = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_FAJR, true)
            silentDhuhr = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_DHUHR, true)
            silentAsr = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_ASR, true)
            silentMaghrib = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_MAGHRIB, true)
            silentIsha = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_SILENT_ISHA, true)
        }
    }

    Dialog(
        onDismissRequest = {
            audioPlayer.stopSound()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides (if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr),
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = CairoFontFamily)
        ) {
            ProvideTextStyle(value = TextStyle(fontFamily = CairoFontFamily)) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 500.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1) Header (Fixed at top)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = if (isAr) "أيقونة الإعدادات" else "Settings Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "\u200EAdhan+",
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isAr) "تطبيق أوقات الصلاة والقرآن الكريم" else "Prayer Times & the Holy Quran",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    audioPlayer.stopSound()
                                    onDismiss()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isAr) "إغلاق النافذة" else "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        // 2) The 6 Structured Sections
                        val soundNameAr = PRESET_ADHAN_SOUNDS.find { it.id == currentSoundId }?.nameAr
                            ?: activeCustomName ?: "صوت الأذان الافتراضي"
                        val soundNameEn = when (currentSoundId) {
                            "makkah" -> "Makkah Grand Mosque Adhan · Fajr format"
                            "madinah" -> "Madinah Prophet's Mosque Adhan · Fajr format"
                            else -> (PRESET_ADHAN_SOUNDS.find { it.id == currentSoundId }?.nameEn ?: activeCustomName ?: "Default Adhan Sound") + " · Fajr format"
                        }
                        val soundSubtitle = if (isAr) soundNameAr else soundNameEn

                        val enabledAdhansCount = listOf(
                            prayerAdhanSettings.fajr,
                            prayerAdhanSettings.dhuhr,
                            prayerAdhanSettings.asr,
                            prayerAdhanSettings.maghrib,
                            prayerAdhanSettings.isha
                        ).count { it }
                        val adhansSubtitle = if (isAr) {
                            if (enabledAdhansCount == 5) "5 صلوات مفعّلة"
                            else if (enabledAdhansCount == 0) "التنبيهات معطلة"
                            else "$enabledAdhansCount صلوات مفعّلة"
                        } else {
                            when (enabledAdhansCount) {
                                0 -> "No prayers enabled"
                                1 -> "1 prayer enabled"
                                else -> "$enabledAdhansCount prayers enabled"
                            }
                        }

                        val currentCalcMethodName = if (isAr) {
                            COMMON_CALCULATION_METHODS.find { it.id == calculationMethodId }?.nameAr ?: "رابطة العالم الإسلامي"
                        } else {
                            when (calculationMethodId) {
                                16 -> "Ministry of Religious Affairs and Awqaf (Algeria)"
                                4 -> "Umm al-Qura University, Makkah"
                                5 -> "Egyptian General Authority of Survey"
                                3 -> "Muslim World League (MWL)"
                                2 -> "Islamic Society of North America (ISNA)"
                                12 -> "Union of Islamic Organizations of France (UOIF)"
                                13 -> "Presidency of Religious Affairs, Turkey (Diyanet)"
                                1 -> "University of Islamic Sciences, Karachi"
                                else -> COMMON_CALCULATION_METHODS.find { it.id == calculationMethodId }?.nameAr ?: "Muslim World League (MWL)"
                            }
                        }

                        val asrSubtitle = if (isAr) {
                            if (asrMethod == "hanafi") "المذهب الحنفي" else "جمهور العلماء (شافعي/مالكي/حنبلي)"
                        } else {
                            if (asrMethod == "hanafi") "Hanafi School" else "Majority of Scholars (Standard)"
                        }

                        val hijriSubtitle = when {
                            hijriOffset > 0 -> if (isAr) "+$hijriOffset يوم" else "+$hijriOffset ${if (hijriOffset == 1) "day" else "days"}"
                            hijriOffset < 0 -> if (isAr) "$hijriOffset يوم" else "$hijriOffset ${if (hijriOffset == -1) "day" else "days"}"
                            else -> if (isAr) "التقويم المعتمد (0)" else "0 days"
                        }

                        val themeSubtitle = if (isAr) {
                            if (appThemeStyle == "night") "ليلي" else "زمردي"
                        } else {
                            if (appThemeStyle == "night") "Night" else "Emerald"
                        }

                        val numeralSubtitle = if (isAr) {
                            if (numeralSystem == "eastern") "الأرقام المشرقية (الهندية: ١، ٢، ٣)" else "الأرقام العربية (الغبارية: 1, 2, 3)"
                        } else {
                            if (numeralSystem == "eastern") "Eastern Arabic numerals (١، ٢، ٣)" else "Western Arabic numerals (1, 2, 3)"
                        }

                        val sections = listOf(
                            SettingSectionData(
                                id = "location",
                                title = if (isAr) "الموقع" else "Location",
                                rows = listOf(
                                    SettingRowData(
                                        id = "location",
                                        title = if (isAr) "تحديد الموقع الجغرافي" else "Set Location",
                                        subtitle = if (cityName.isNotEmpty()) {
                                            if (isAr) "$cityName، $countryName"
                                            else "${com.example.ui.util.AppLocalization.getLocalizedCityName(cityName, appLanguage)}, ${com.example.ui.util.AppLocalization.getLocalizedCountryName(countryName, appLanguage)}"
                                        } else {
                                            if (isAr) "تحديد الموقع التلقائي أو اليدوي" else "Set automatic or manual location"
                                        },
                                        icon = Icons.Default.LocationOn,
                                        onClick = { onOpenLocationDialog() }
                                    ),
                                    SettingRowData(
                                        id = "mosque",
                                        title = if (isAr) "البحث عن أقرب مسجد" else "Nearest Mosque",
                                        subtitle = if (isAr) "المساجد القريبة منك عبر الخريطة" else "Nearby mosques via maps",
                                        icon = Icons.Default.Place,
                                        onClick = { onFindNearestMosque() }
                                    )
                                )
                            ),
                            SettingSectionData(
                                id = "adhan",
                                title = if (isAr) "الأذان والتنبيهات" else "Adhan & Alerts",
                                rows = listOf(
                                    SettingRowData(
                                        id = "sounds",
                                        title = if (isAr) "الأصوات" else "Sounds",
                                        subtitle = soundSubtitle,
                                        icon = Icons.Default.MusicNote,
                                        onClick = { activeSubDialog = "sounds" }
                                    ),
                                    SettingRowData(
                                        id = "adhan_alerts",
                                        title = if (isAr) "تنبيهات الأذان" else "Adhan Alerts",
                                        subtitle = adhansSubtitle,
                                        icon = Icons.Default.Notifications,
                                        onClick = { activeSubDialog = "adhan_alerts" }
                                    ),
                                    SettingRowData(
                                        id = "auto_silent",
                                        title = if (isAr) "الوضع الصامت أثناء الصلاة 🔕" else "Prayer Auto-Silent Mode 🔕",
                                        subtitle = if (autoSilentEnabled) {
                                            val modeDesc = if (autoSilentModeType == "vibrate") (if (isAr) "اهتزاز" else "Vibrate") else (if (isAr) "صامت تام" else "Silent")
                                            if (isAr) "مفعّل ($modeDesc - مدة الصلاة $autoSilentDuration د)" else "Enabled ($modeDesc - $autoSilentDuration min)"
                                        } else {
                                            if (isAr) "معطّل - تحويل الهاتف للصامت تلقائياً بعد الأذان" else "Disabled - Mute phone automatically after Adhan"
                                        },
                                        icon = Icons.Default.NotificationsOff,
                                        onClick = {
                                            hasPolicyAccess = PrayerAutoSilentManager.hasNotificationPolicyAccess(context)
                                            activeSubDialog = "auto_silent"
                                        }
                                    ),
                                    SettingRowData(
                                        id = "lockscreen_notif",
                                        title = if (isAr) "إشعار شاشة القفل ولوحة الإشعارات" else "Lock Screen & Notification Shade",
                                        subtitle = if (showLockScreenPrayerNotification) {
                                            if (isAr) "عرض الصلاة القادمة والعد التنازلي بشكل دائم" else "Permanent next prayer & live countdown"
                                        } else {
                                            if (isAr) "معطّل" else "Disabled"
                                        },
                                        icon = Icons.Default.Notifications,
                                        onClick = null,
                                        trailingContent = {
                                            Switch(
                                                checked = showLockScreenPrayerNotification,
                                                onCheckedChange = { onToggleLockScreenPrayerNotification(it) }
                                            )
                                        }
                                    ),
                                    SettingRowData(
                                        id = "override_silent",
                                        title = if (isAr) "تشغيل في الوضع الصامت" else "Play in Silent Mode",
                                        subtitle = if (overrideSilentMode) {
                                            if (isAr) "ينطلق الأذان حتى لو كان الهاتف صامتاً" else "Adhan plays even when the phone is on silent."
                                        } else {
                                            if (isAr) "معطّل في الوضع الصامت" else "Disabled in silent mode."
                                        },
                                        icon = Icons.Default.VolumeUp,
                                        onClick = null,
                                        trailingContent = {
                                            Switch(checked = overrideSilentMode, onCheckedChange = { onToggleOverrideSilentMode(it) })
                                        }
                                    ),
                                    SettingRowData(
                                        id = "flip_mute",
                                        title = if (isAr) "إيقاف الأذان بقلب الهاتف" else "Flip to Silence Adhan",
                                        subtitle = if (useFlipToMute) {
                                            if (isAr) "اقلب الهاتف على شاشته لإسكات الأذان" else "Flip your phone face down to stop the adhan."
                                        } else {
                                            if (isAr) "معطّل" else "Disabled."
                                        },
                                        icon = Icons.Default.ScreenRotation,
                                        onClick = null,
                                        trailingContent = {
                                            Switch(checked = useFlipToMute, onCheckedChange = { onToggleFlipToMute(it) })
                                        }
                                    ),
                                    SettingRowData(
                                        id = "adhan_audio_stream",
                                        title = if (isAr) "قناة التحكم بصوت الأذان" else "Adhan Volume Stream",
                                        subtitle = if (adhanAudioStreamType == "alarm") {
                                            if (isAr) "قناة المنبه (Alarm) - ينطلق حتى في الوضع الصامت" else "Alarm stream - Rings even in silent mode"
                                        } else {
                                            if (isAr) "قناة الوسائط (Media) - يتبع صوت المقاطع والفيديو" else "Media stream - Follows media & video volume"
                                        },
                                        icon = Icons.Default.Tune,
                                        onClick = {
                                            activeSubDialog = "adhan_audio_stream"
                                        }
                                    )
                                )
                            ),
                            SettingSectionData(
                                id = "storage",
                                title = if (isAr) "المصحف والتخزين" else "Quran & Storage",
                                rows = listOf(
                                    SettingRowData(
                                        id = "storage_location",
                                        title = if (isAr) "مكان التخزين وتنزيل السور" else "Storage Location & Surah Downloads",
                                        subtitle = if (useSdCardForDownloads) {
                                            if (isAr) "بطاقة الذاكرة الخارجية (SD Card)" else "External memory card (SD card)"
                                        } else {
                                            if (isAr) "ذاكرة الهاتف الداخلية" else "Internal phone storage"
                                        },
                                        icon = Icons.Default.FolderOpen,
                                        onClick = { activeSubDialog = "storage_location" }
                                    )
                                )
                            ),
                            SettingSectionData(
                                id = "calculation",
                                title = if (isAr) "حساب المواقيت" else "Prayer Calculation",
                                rows = listOf(
                                    SettingRowData(
                                        id = "calc_method",
                                        title = if (isAr) "طريقة حساب المواقيت" else "Calculation Method",
                                        subtitle = currentCalcMethodName,
                                        icon = Icons.Default.AccessTime,
                                        onClick = { activeSubDialog = "calc_method" }
                                    ),
                                    SettingRowData(
                                        id = "asr_school",
                                        title = if (isAr) "مذهب حساب وقت العصر" else "Asr Calculation",
                                        subtitle = asrSubtitle,
                                        icon = Icons.Default.Tune,
                                        onClick = { activeSubDialog = "asr_school" }
                                    ),
                                    SettingRowData(
                                        id = "minute_adjust",
                                        title = if (isAr) "تعديل دقائق الصلوات" else "Prayer Minute Offsets",
                                        subtitle = if (isAr) "الضبط الدقيق لمواقيت الصلاة" else "Fine-tune prayer times",
                                        icon = Icons.Default.AccessTime,
                                        onClick = { onOpenPrayerOffsets() }
                                    ),
                                    SettingRowData(
                                        id = "hijri_adjust",
                                        title = if (isAr) "تعديل التقويم الهجري" else "Hijri Date Adjustment",
                                        subtitle = hijriSubtitle,
                                        icon = Icons.Default.CalendarToday,
                                        onClick = { activeSubDialog = "hijri_adjust" }
                                    )
                                )
                            ),
                            SettingSectionData(
                                id = "appearance",
                                title = if (isAr) "المظهر واللغة" else "Appearance & Language",
                                rows = listOf(
                                    SettingRowData(
                                        id = "language",
                                        title = if (isAr) "اللغة" else "Language",
                                        subtitle = if (appLanguage == "en") "English" else "العربية",
                                        icon = Icons.Default.Language,
                                        onClick = null,
                                        trailingContent = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                val isArabic = appLanguage != "en"
                                                Surface(onClick = { onSelectLanguage("ar") }, shape = RoundedCornerShape(18.dp), color = if (isArabic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (isArabic) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.defaultMinSize(minHeight = 32.dp)) { Text("العربية", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
                                                Surface(onClick = { onSelectLanguage("en") }, shape = RoundedCornerShape(18.dp), color = if (!isArabic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (!isArabic) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.defaultMinSize(minHeight = 32.dp)) { Text("English", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
                                            }
                                        }
                                    ),
                                    SettingRowData(
                                        id = "theme",
                                        title = if (isAr) "شكل التطبيق (الثيم)" else "App Theme",
                                        subtitle = themeSubtitle,
                                        icon = Icons.Default.Palette,
                                        onClick = { activeSubDialog = "theme" }
                                    ),
                                    SettingRowData(
                                        id = "numeral_system",
                                        title = if (isAr) "نظام الأرقام" else "Number System",
                                        subtitle = numeralSubtitle,
                                        icon = Icons.Default.FormatSize,
                                        onClick = { activeSubDialog = "numeral_system" }
                                    ),
                                    SettingRowData(
                                        id = "time_format",
                                        title = if (isAr) "نظام عرض الوقت (12 / 24 ساعة)" else "Time Format (12h / 24h)",
                                        subtitle = if (prayerAdhanSettings.is24HourFormat) {
                                            if (isAr) "نظام 24 ساعة (مثال: 18:32)" else "24-hour format (e.g., 18:32)"
                                        } else {
                                            if (isAr) "نظام 12 ساعة مع (ص / م)" else "12-hour format with (AM / PM)"
                                        },
                                        icon = Icons.Default.AccessTime,
                                        onClick = { onToggle24HourFormat(!prayerAdhanSettings.is24HourFormat) },
                                        trailingContent = {
                                            Switch(
                                                checked = prayerAdhanSettings.is24HourFormat,
                                                onCheckedChange = { onToggle24HourFormat(it) }
                                            )
                                        }
                                    ),
                                    SettingRowData(
                                        id = "dark_mode",
                                        title = if (isAr) "الوضع الداكن" else "Dark Mode",
                                        subtitle = if (isDarkMode) {
                                            if (isAr) "الوضع الليلي مفعّل" else "Dark mode enabled."
                                        } else {
                                            if (isAr) "الوضع الفاتح مفعّل" else "Light mode enabled."
                                        },
                                        icon = Icons.Default.DarkMode,
                                        onClick = null,
                                        trailingContent = {
                                            Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                                        }
                                    ),
                                    SettingRowData(
                                        id = "mosque_backgrounds",
                                        title = if (isAr) "خلفيات المساجد المتغيرة" else "Dynamic Mosque Backgrounds",
                                        subtitle = if (showMosqueBackgrounds) {
                                            if (isAr) "صور مساجد حول العالم تتغير تلقائياً مع كل صلاة" else "Mosque photos change automatically with each prayer."
                                        } else {
                                            if (isAr) "معطلة (استخدام الخلفية الكلاسيكية)" else "Disabled (using classic background)."
                                        },
                                        icon = Icons.Default.Mosque,
                                        onClick = null,
                                        trailingContent = {
                                            Switch(
                                                checked = showMosqueBackgrounds,
                                                onCheckedChange = { isChecked ->
                                                    onToggleMosqueBackgrounds(isChecked)
                                                }
                                            )
                                        }
                                    )
                                )
                            ),
                            SettingSectionData(
                                id = "about",
                                title = if (isAr) "عن التطبيق" else "About",
                                rows = listOf(
                                    SettingRowData(
                                        id = "contact",
                                        title = if (isAr) "تواصل معي" else "Contact the Developer",
                                        subtitle = "Cherifsouigat@gmail.com",
                                        icon = Icons.Default.Email,
                                        onClick = { activeSubDialog = "contact" }
                                    ),
                                    SettingRowData(
                                        id = "about",
                                        title = if (isAr) "حول التطبيق" else "About the App",
                                        subtitle = if (isAr) "معلومات عن تطبيق ‎Adhan+" else "Information about ‎Adhan+ app",
                                        icon = Icons.Default.Info,
                                        onClick = { activeSubDialog = "about" }
                                    ),
                                    SettingRowData(
                                        id = "rate",
                                        title = if (isAr) "قيم التطبيق" else "Rate the App",
                                        subtitle = if (isAr) "شاركنا رأيك في المتجر" else "Share your review on the store",
                                        icon = Icons.Default.Star,
                                        onClick = { activeSubDialog = "rate" }
                                    ),
                                    SettingRowData(
                                        id = "share",
                                        title = if (isAr) "مشاركة التطبيق" else "Share the App",
                                        subtitle = if (isAr) "شارك التطبيق مع أصدقائك وعائلتك" else "Share the app with friends and family",
                                        icon = Icons.Default.Share,
                                        onClick = {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                 type = "text/plain"
                                                 putExtra(Intent.EXTRA_SUBJECT, if (isAr) "تطبيق Adhan+" else "Adhan+ App")
                                                 putExtra(
                                                     Intent.EXTRA_TEXT,
                                                     if (isAr) "تطبيق Adhan+ - لمواقيت الصلاة الدقيقة، اتجاه القبلة، والقرآن الكريم والأذكار.\n\nحمل التطبيق الآن:\nhttps://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file"
                                                     else "Adhan+ - Accurate prayer times, Qibla compass, holy Quran and daily reminders.\n\nDownload app now:\nhttps://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file"
                                                 )
                                                 flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                             }
                                            try {
                                                context.startActivity(
                                                    Intent.createChooser(sendIntent, if (isAr) "مشاركة تطبيق ‎Adhan+" else "Share ‎Adhan+ App").apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                )
                                            } catch (_: Exception) {}
                                        }
                                    ),
                                    SettingRowData(
                                        id = "check_updates",
                                        title = if (isAr) "التحقق من وجود تحديثات" else "Check for Updates",
                                        subtitle = if (isCheckingUpdatesState) {
                                            if (isAr) "جارِ فحص خادم التحديثات..." else "Checking update server..."
                                        } else {
                                            if (isAr) "فحص توفر إصدار أحدث (الحالي: v$installedVersionName)"
                                            else "Check for newer version (Current: v$installedVersionName)"
                                        },
                                        icon = Icons.Default.SystemUpdate,
                                        onClick = {
                                            if (!isCheckingUpdatesState) {
                                                isCheckingUpdatesState = true
                                                Toast.makeText(context, if (isAr) "جارِ فحص التحديثات..." else "Checking for updates...", Toast.LENGTH_SHORT).show()
                                                coroutineScope.launch {
                                                    val updateResult = com.example.util.AppUpdateManager.checkForUpdate(context, isManual = true)
                                                    isCheckingUpdatesState = false
                                                    when (updateResult.status) {
                                                        com.example.util.AppUpdateStatus.UPDATE_AVAILABLE -> {
                                                            manualUpdateDialogInfo = updateResult.manifest
                                                        }
                                                        com.example.util.AppUpdateStatus.UP_TO_DATE -> {
                                                            Toast.makeText(
                                                                context,
                                                                if (isAr) "أنت تستخدم أحدث إصدار متوفر (v$installedVersionName) ✔"
                                                                else "You are using the latest version (v$installedVersionName) ✔",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                        com.example.util.AppUpdateStatus.NO_INTERNET -> {
                                                            Toast.makeText(
                                                                context,
                                                                if (isAr) updateResult.errorMessageAr ?: "لا يوجد اتصال بالإنترنت"
                                                                else updateResult.errorMessageEn ?: "No internet connection",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        com.example.util.AppUpdateStatus.SERVER_ERROR -> {
                                                            Toast.makeText(
                                                                context,
                                                                if (isAr) updateResult.errorMessageAr ?: "تعذّر الوصول إلى خادم التحديثات"
                                                                else updateResult.errorMessageEn ?: "Server unreachable",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        com.example.util.AppUpdateStatus.INVALID_MANIFEST -> {
                                                            Toast.makeText(
                                                                context,
                                                                if (isAr) updateResult.errorMessageAr ?: "رابط التحديث غير صالح"
                                                                else updateResult.errorMessageEn ?: "Invalid update manifest",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    ),
                                    SettingRowData(
                                        id = "privacy",
                                        title = if (isAr) "شروط الاستخدام والخصوصية" else "Terms & Privacy Policy",
                                        subtitle = if (isAr) "سياسة الخصوصية وأمان البيانات" else "Privacy policy and data safety",
                                        icon = Icons.Default.Book,
                                        onClick = { activeSubDialog = "privacy" }
                                    )
                                )
                            )
                        )

                        sections.forEach { section ->
                            SettingsSectionNew(
                                sectionTitle = section.title,
                                rows = section.rows,
                                isExpanded = expandedSections.contains(section.id),
                                onToggle = {
                                    expandedSections = if (expandedSections.contains(section.id)) {
                                        expandedSections - section.id
                                    } else {
                                        expandedSections + section.id
                                    }
                                },
                                isAr = isAr
                            )
                        }

                        // Version Number (Static text at bottom center)
                        Text(
                            text = if (isAr) "الإصدار v$installedVersionName" else "Version v$installedVersionName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // ==================== SUB-DIALOGS ====================

    // 1. Sounds SubDialog
    if (activeSubDialog == "sounds") {
        SettingsSubDialogContainer(
            title = if (isAr) "أصوات الأذان" else "Sounds",
            icon = Icons.Default.MusicNote,
            onDismiss = {
                audioPlayer.stopSound()
                playingUrl = null
                activeSubDialog = null
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isAr) "اختر المؤذن المفضل:" else "Choose preferred muezzin:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                PRESET_ADHAN_SOUNDS.forEach { sound ->
                    val isSelected = currentSoundId == sound.id
                    val isStandardPlaying = playingUrl == sound.audioUrl

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentSoundId = sound.id
                                onSaveAdhanSound(sound.id, activeCustomUri, activeCustomName)
                            }
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
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isAr) sound.nameAr else sound.nameEn,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isStandardPlaying) {
                                        audioPlayer.stopSound()
                                        playingUrl = null
                                    } else {
                                        playingUrl = sound.audioUrl
                                        audioPlayer.playSound(sound.audioUrl) {
                                            playingUrl = null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStandardPlaying) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isStandardPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isAr) "معاينة صوت الأذان" else "Preview adhan sound",
                                    tint = if (isStandardPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )


            }
        }
    }

    // 2. Adhan Alerts SubDialog
    if (activeSubDialog == "adhan_alerts") {
        SettingsSubDialogContainer(
            title = if (isAr) "تنبيهات الأذان" else "Adhan Alerts",
            icon = Icons.Default.Notifications,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrayerToggleRow(
                    prayerNameAr = "الفجر",
                    prayerNameEn = "Fajr",
                    emoji = "🌙",
                    isEnabled = prayerAdhanSettings.fajr,
                    isAr = isAr,
                    onToggle = { onTogglePrayerAdhan("fajr", it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                PrayerToggleRow(
                    prayerNameAr = "الظهر",
                    prayerNameEn = "Dhuhr",
                    emoji = "☀️",
                    isEnabled = prayerAdhanSettings.dhuhr,
                    isAr = isAr,
                    onToggle = { onTogglePrayerAdhan("dhuhr", it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                PrayerToggleRow(
                    prayerNameAr = "العصر",
                    prayerNameEn = "Asr",
                    emoji = "🌤️",
                    isEnabled = prayerAdhanSettings.asr,
                    isAr = isAr,
                    onToggle = { onTogglePrayerAdhan("asr", it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                PrayerToggleRow(
                    prayerNameAr = "المغرب",
                    prayerNameEn = "Maghrib",
                    emoji = "🌅",
                    isEnabled = prayerAdhanSettings.maghrib,
                    isAr = isAr,
                    onToggle = { onTogglePrayerAdhan("maghrib", it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                PrayerToggleRow(
                    prayerNameAr = "العشاء",
                    prayerNameEn = "Isha",
                    emoji = "🌌",
                    isEnabled = prayerAdhanSettings.isha,
                    isAr = isAr,
                    onToggle = { onTogglePrayerAdhan("isha", it) }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (isAr) "إشعار الصلاة في شاشة القفل ولوحة الإشعارات" else "Lock Screen & Notification Shade",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAr) "عرض الصلاة القادمة والعد التنازلي بشكل دائم" else "Permanent next prayer & live countdown",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showLockScreenPrayerNotification,
                        onCheckedChange = { onToggleLockScreenPrayerNotification(it) }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = if (isAr) "نمط الإشعار والمنبه:" else "Notification Style:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSelectNotificationStyle("POPUP") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (notificationStyle == "POPUP") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isAr) "نافذة وأذان كامل" else "Pop-up with full Adhan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notificationStyle == "POPUP") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onSelectNotificationStyle("NOTIFICATION") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (notificationStyle == "NOTIFICATION") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isAr) "إشعار هادئ" else "Quiet notification",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notificationStyle == "NOTIFICATION") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 2.5 Auto-Silent SubDialog
    if (activeSubDialog == "auto_silent") {
        PrayerAutoSilentDialog(
            isAr = isAr,
            onDismiss = { activeSubDialog = null },
            onConfirmed = {
                autoSilentEnabled = silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_ENABLED, false)
                autoSilentModeType = silentPrefs.getString(PrayerAutoSilentManager.KEY_MODE_TYPE, "silent") ?: "silent"
                autoSilentDuration = silentPrefs.getInt(PrayerAutoSilentManager.KEY_DURATION_MINUTES, 20)
                activeSubDialog = null
            }
        )
    }

    // 2.6 Adhan Audio Stream SubDialog
    if (activeSubDialog == "adhan_audio_stream") {
        SettingsSubDialogContainer(
            title = if (isAr) "قناة التحكم بصوت الأذان" else "Adhan Volume Stream",
            icon = Icons.Default.Tune,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isAr)
                        "حدد مسار الصوت في نظام أندرويد الذي يتحكم في صوت الأذان:"
                    else
                        "Select the Android audio stream that controls Adhan playback:",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                // Option 1: Alarm Stream
                val isAlarm = adhanAudioStreamType == "alarm"
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAlarm) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(
                        width = if (isAlarm) 1.5.dp else 1.dp,
                        color = if (isAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSetAdhanAudioStreamType("alarm")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isAlarm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isAr) "قناة المنبه والتنبيه (Alarm)" else "Alarm Stream",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isAr) "موصى به" else "Recommended",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr)
                                    "يتبع شريط صوت المنبه في هاتفك. يضمن انطلاق الأذان حتى لو كان الهاتف في الوضع الصامت أو الاهتزاز."
                                else
                                    "Controlled by device Alarm volume. Ensures Adhan rings even if the phone is on silent or vibrate.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        if (isAlarm) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Option 2: Media Stream
                val isMedia = !isAlarm
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isMedia) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(
                        width = if (isMedia) 1.5.dp else 1.dp,
                        color = if (isMedia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSetAdhanAudioStreamType("media")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isMedia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isMedia) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) "قناة الوسائط والموسيقى (Media)" else "Media Stream",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr)
                                    "يتبع شريط صوت المقاطع والفيديوهات ومكبرات البلوتوث. لن يرن الأذان إذا كان مستوى صوت الوسائط مغلقاً."
                                else
                                    "Controlled by device Media volume (music & videos). Will not ring if media volume is muted.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        if (isMedia) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = { activeSubDialog = null },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isAr) "تم" else "Done",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }

    // 3. Calculation Method SubDialog
    if (activeSubDialog == "calc_method") {
        SettingsSubDialogContainer(
            title = if (isAr) "طريقة حساب المواقيت" else "Calculation Method",
            icon = Icons.Default.AccessTime,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                COMMON_CALCULATION_METHODS.forEach { method ->
                    val isSelected = calculationMethodId == method.id
                    val methodName = if (isAr) method.nameAr else when (method.id) {
                        16 -> "Ministry of Religious Affairs and Awqaf (Algeria)"
                        4 -> "Umm al-Qura University, Makkah"
                        5 -> "Egyptian General Authority of Survey"
                        3 -> "Muslim World League (MWL)"
                        2 -> "Islamic Society of North America (ISNA)"
                        12 -> "Union of Islamic Organizations of France (UOIF)"
                        13 -> "Presidency of Religious Affairs, Turkey (Diyanet)"
                        1 -> "University of Islamic Sciences, Karachi"
                        else -> method.nameAr
                    }
                    val regionDesc = if (isAr) method.regionAr else when (method.id) {
                        16 -> "Algeria (official default)"
                        4 -> "Saudi Arabia and the Gulf states"
                        5 -> "Egypt, Syria, Lebanon and parts of Africa"
                        3 -> "Europe, the Far East and most countries"
                        2 -> "United States and Canada"
                        12 -> "France and parts of Western Europe"
                        13 -> "Turkey"
                        1 -> "Pakistan, Bangladesh, India and parts of Afghanistan"
                        else -> method.regionAr
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectCalculationMethod(method.id)
                                activeSubDialog = null
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = methodName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = regionDesc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Asr School SubDialog
    if (activeSubDialog == "asr_school") {
        SettingsSubDialogContainer(
            title = if (isAr) "مذهب حساب وقت العصر" else "Asr Calculation",
            icon = Icons.Default.Tune,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val isStandard = asrMethod != "hanafi"

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isStandard) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectAsrMethod("standard")
                            activeSubDialog = null
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isAr) "جمهور العلماء (القياسي)" else "Majority of Scholars (Standard)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(if (isAr) "الشافعي والمالكي والحنبلي" else "Shafi'i, Maliki and Hanbali", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isStandard) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isStandard) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectAsrMethod("hanafi")
                            activeSubDialog = null
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isAr) "المذهب الحنفي" else "Hanafi School", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(if (isAr) "يتأخر وقت العصر حتى يصير ظل الشيء مثليه" else "Asr begins later, when an object's shadow is twice its length", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!isStandard) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // 5. Hijri Adjustment SubDialog
    if (activeSubDialog == "hijri_adjust") {
        SettingsSubDialogContainer(
            title = if (isAr) "تعديل التقويم الهجري" else "Hijri Date Adjustment",
            icon = Icons.Default.CalendarToday,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayHijriDate = remember(hijriDate, appLanguage) { com.example.ui.util.AppLocalization.getLocalizedHijriDate(hijriDate, appLanguage) }
                Text(
                    text = if (isAr) "التاريخ الهجري المحسوب: ${displayHijriDate.ifBlank { "جاري الحساب..." }}" else "Calculated Hijri Date: ${displayHijriDate.ifBlank { "Calculating..." }}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (isAr) "يمكنك إضافة أو إنقاص أيام ليتطابق مع الرؤية المحلية:" else "Adjust days to align with local moon sighting:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        -2 to (if (isAr) "-2 يوم" else "-2 days"),
                        -1 to (if (isAr) "-1 يوم" else "-1 day"),
                        0 to (if (isAr) "0" else "0 days"),
                        1 to (if (isAr) "+1 يوم" else "+1 day"),
                        2 to (if (isAr) "+2 يوم" else "+2 days")
                    ).forEach { (offset, label) ->
                        val isSelected = hijriOffset == offset
                        Button(
                            onClick = { onUpdateHijriOffset(offset) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // 6. App Theme SubDialog
    if (activeSubDialog == "theme") {
        SettingsSubDialogContainer(
            title = if (isAr) "شكل التطبيق (الثيم)" else "App Theme",
            icon = Icons.Default.Palette,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isAr) "اختر الثيم المفضل:" else "Choose preferred theme:",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Row 1: Emerald & Night
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. زمردي
                    val isEmeraldSelected = appThemeStyle == "emerald"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEmeraldSelected) Color(0xFF0B3B2C) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isEmeraldSelected) 2.5.dp else 1.dp,
                            color = if (isEmeraldSelected) Color(0xFFF5C84C) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("emerald") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF0B3B2C)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFF5C84C)))
                            }
                            Text(
                                text = if (isAr) "زمردي 🌿" else "Emerald 🌿",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEmeraldSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 2. ليلي
                    val isNightSelected = appThemeStyle == "night"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNightSelected) Color(0xFF0C2340) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isNightSelected) 2.5.dp else 1.dp,
                            color = if (isNightSelected) Color(0xFFFFC857) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("night") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF0C2340)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFFFC857)))
                            }
                            Text(
                                text = if (isAr) "ليلي 🌙" else "Night 🌙",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 2: Gold & Turquoise
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. ذهبي
                    val isGoldSelected = appThemeStyle == "gold"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isGoldSelected) Color(0xFF7A5200) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isGoldSelected) 2.5.dp else 1.dp,
                            color = if (isGoldSelected) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("gold") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF7A5200)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFFEF3C7)))
                            }
                            Text(
                                text = if (isAr) "ذهبي 🌟" else "Gold 🌟",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoldSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 4. تركوازي
                    val isTurquoiseSelected = appThemeStyle == "turquoise"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTurquoiseSelected) Color(0xFF0D5C63) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isTurquoiseSelected) 2.5.dp else 1.dp,
                            color = if (isTurquoiseSelected) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("turquoise") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF0D5C63)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFE0F2FE)))
                            }
                            Text(
                                text = if (isAr) "تركوازي 💧" else "Turquoise 💧",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTurquoiseSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 3: Purple & Bright Gold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 5. أرجواني
                    val isPurpleSelected = appThemeStyle == "purple"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPurpleSelected) Color(0xFF4A1525) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isPurpleSelected) 2.5.dp else 1.dp,
                            color = if (isPurpleSelected) Color(0xFFFCE7F3) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("purple") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF4A1525)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFFCE7F3)))
                            }
                            Text(
                                text = if (isAr) "أرجواني 🔮" else "Purple 🔮",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPurpleSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 6. ذهبي براق
                    val isBrightGoldSelected = appThemeStyle == "bright_gold"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBrightGoldSelected) Color(0xFFC59B27) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isBrightGoldSelected) 2.5.dp else 1.dp,
                            color = if (isBrightGoldSelected) Color(0xFFFFF3CD) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("bright_gold") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFC59B27)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFFFF3CD)))
                            }
                            Text(
                                text = if (isAr) "ذهبي براق ✨" else "Bright Gold ✨",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBrightGoldSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Row 4: Pale Grey
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 7. رمادي باهت
                    val isPaleGreySelected = appThemeStyle == "pale_grey"
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPaleGreySelected) Color(0xFF5A6578) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isPaleGreySelected) 2.5.dp else 1.dp,
                            color = if (isPaleGreySelected) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectThemeStyle("pale_grey") }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF5A6578)).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
                            }
                            Text(
                                text = if (isAr) "رمادي باهت 🪙" else "Pale Grey 🪙",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaleGreySelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Spacer card to keep grid balanced
                    Spacer(modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "اتباع نظام الهاتف" else "Match system",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAr) "التبديل التلقائي وفقاً لوضع جهازك (فاتح / داكن)" else "Switch automatically based on device theme (light / dark)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = followSystemDarkMode,
                        onCheckedChange = { onToggleFollowSystemDarkMode(it) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "الوضع الداكن اليدوي" else "Dark Mode",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDarkMode) (if (isAr) "الوضع الداكن مفعّل" else "Dark mode enabled.") else (if (isAr) "الوضع الفاتح مفعّل" else "Light mode enabled."),
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() }
                    )
                }
            }
        }
    }

    // 7. Contact Us SubDialog
    if (activeSubDialog == "contact") {
        val contactEmail = "Cherifsouigat@gmail.com"
        SettingsSubDialogContainer(
            title = if (isAr) "تواصل معي" else "Contact the Developer",
            icon = Icons.Default.Email,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isAr) "البريد الإلكتروني المباشر:" else "Direct Email Address:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$contactEmail")
                                    putExtra(Intent.EXTRA_SUBJECT, if (isAr) "تطبيق Adhan+ - استفسار / اقتراح" else "‎Adhan+ App - Inquiry / Suggestion")
                                }
                                context.startActivity(Intent.createChooser(intent, if (isAr) "إرسال رسالة بريد إلكتروني" else "Send Email"))
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, contactEmail, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = contactEmail,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$contactEmail")
                                    putExtra(Intent.EXTRA_SUBJECT, if (isAr) "تطبيق Adhan+ - استفسار / اقتراح" else "‎Adhan+ App - Inquiry / Suggestion")
                                }
                                context.startActivity(Intent.createChooser(intent, if (isAr) "إرسال رسالة بريد إلكتروني" else "Send Email"))
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, contactEmail, android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isAr) "فتح تطبيق البريد ✉️" else "Open Email App ✉️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("email", contactEmail)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, if (isAr) "تم نسخ البريد الإلكتروني بنجاح!" else "Email copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isAr) "نسخ البريد 📋" else "Copy Email 📋", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 8. About SubDialog
    if (activeSubDialog == "about") {
        SettingsSubDialogContainer(
            title = if (isAr) "حول التطبيق ودليل الاستخدام" else "About the App & User Guide",
            icon = Icons.Default.Info,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Banner Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isAr) "تطبيق ‎Adhan+ (أذان بلس)" else "‎Adhan+ Application",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isAr) "الإصدار 1.5 · رفيقك الإسلامي الشامل" else "Version 1.5 · Your Islamic Companion",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(
                            text = if (isAr)
                                "تطبيق إسلامي شامل ومجاني، صُمم بعناية فائقة ليعمل بأعلى دقة وبدون إنترنت، مع تجربة مستخدم عصرية وهادئة ترافقك في عباداتك اليومية."
                            else
                                "A comprehensive and free Islamic application, carefully designed for high precision and offline use, providing a modern and serene experience.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Section Guide Title
                Text(
                    text = if (isAr) "📖 دليل التحكم واستخدام أقسام التطبيق:" else "📖 How to Control & Use the App:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // 1. Prayer Times & Adhan Control Card
                AboutGuideCard(
                    title = if (isAr) "🕌 مواقيت الصلاة والأذان التلقائي" else "🕌 Prayer Times & Automated Adhan",
                    items = if (isAr) listOf(
                        "تحديد الموقع: يدعم التحديد التلقائي الدقيق عبر GPS أو اختيار مدينتك يدوياً من قائمة المدن، مع إمكانية تثبيت الموقع.",
                        "فروق التوقيت: يمكنك تقديم أو تأخير أي صلاة بالدقائق ليتطابق التوقيت تماماً مع مسجد حيك.",
                        "طرق الحساب والمذهب: اختر هيئة الحساب الفلكي المعتمدة في بلدك ومذهب صلاة العصر (الجمهور أو الحنفي).",
                        "تخصيص أصوات الأذان: اختر من بين أصوات مشاهير المؤذنين، أو خصص أذاناً منفرداً للفجر، أو أضف صوت أذان خاصاً من هاتفك.",
                        "قناة التحكم بالصوت: اختر قناة المنبه (Alarm) لضمان انطلاق الأذان حتى في الوضع الصامت، أو قناة الوسائط (Media).",
                        "إيقاف الأذان بقلب الهاتف: بمجرد قلب الهاتف على وجهه أثناء انطلاق الأذان يتوقف الصوت فوراً دون الحاجة لفتح الشاشة.",
                        "الوضع الصامت أثناء الصلاة (Auto-Silent): يحول هاتفك تلقائياً لوضع الصامت أو الاهتزاز عند حلول الصلاة، ويعيد الصوت الطبيعي فور انتهائها مع نغمة تنبيه خفيفة."
                    ) else listOf(
                        "Location Setting: Automatic GPS detection or manual city selection with pinned location support.",
                        "Minute Offsets: Fine-tune any prayer time by minutes to match your local mosque exactly.",
                        "Calculation Methods: Select official astronomical method and Asr school (Standard or Hanafi).",
                        "Adhan Sounds: Multiple muezzin voices, separate Fajr adhan option, and custom audio file selection.",
                        "Audio Stream: Choose Alarm stream (plays in silent) or Media stream.",
                        "Flip to Silence: Turn your phone face-down to stop the adhan immediately without unlocking.",
                        "Auto-Silent Mode: Automatically mutes device during prayer and restores previous sound level afterwards with a gentle chime."
                    ),
                    isAr = isAr
                )

                // 2. Quran & Recitation Card
                AboutGuideCard(
                    title = if (isAr) "📖 المصحف الشريف والتلاوات" else "📖 Holy Quran & Recitations",
                    items = if (isAr) listOf(
                        "روايات القرآن: إمكانية التبديل السهل بين رواية حفص عن عاصم ورواية ورش عن نافع.",
                        "التلاوات والاستماع: استماع متواصل لسور وآيات القرآن بأصوات نخبة من كبار القراء، مع شريط تحكم كامل بالصوت.",
                        "تفسير وبحث الآيات: عند الضغط على أي آية أثناء القراءة يمكنك البحث المباشر بضغطة زر عن تفسير الآية ومعانيها، مع نسخ الآية وحفظ علامات القراءة.",
                        "تخصيص الخط والعرض: التحكم بحجم الخط، واختيار لون الخط المفضل، مع وضع ملء الشاشة لقراءة مريحة بدون تشتيت.",
                        "التشغيل والتخزين دون إنترنت: تنزيل نصوص وتلاوات السور لتعمل أوفلاين، مع دعم كامل لحفظ التلاوات في الذاكرة الداخلية أو بطاقة الذاكرة الخارجية SD Card مع عرض المساحة المتوفرة."
                    ) else listOf(
                        "Quran Narrations: Seamless switching between Hafs and Warsh recitations.",
                        "Audio Recitations: High-quality recitation audio by renowned reciters with playback controls.",
                        "Verse Tafseer & Search: Tap any verse during reading to search its explanation and meanings with one tap, copy text, or save reading bookmarks.",
                        "Font & Display: Adjust font size, select font colors, and toggle full-screen reading mode.",
                        "Offline Storage: Download surahs and recitations offline, with full support for saving to internal storage or external SD Card with live space calculation."
                    ),
                    isAr = isAr
                )

                // 3. Qibla & Mosques Card
                AboutGuideCard(
                    title = if (isAr) "🧭 القبلة والمساجد القريبة" else "🧭 Qibla Compass & Mosques",
                    items = if (isAr) listOf(
                        "بوصلة القبلة: بوصلة مغناطيسية دقيقة تعرض اتجاه الكعبة المشرفة بدقة مع درجات الزاوية وإرشادات لمعايرة الحساسات.",
                        "البحث عن أقرب مسجد: زر مباشر في الإعدادات للبحث عن المساجد المحيطة بك وعرض الطرق المؤدية إليها عبر تطبيق خرائط Google وفق إحداثيات موقعك."
                    ) else listOf(
                        "Qibla Compass: Accurate magnetic compass pointing directly to the Kaaba with degrees and sensor calibration hints.",
                        "Nearest Mosque: Dedicated button in Settings to search for surrounding mosques and get directions via Google Maps based on your current coordinates."
                    ),
                    isAr = isAr
                )

                // 4. Adhkar & Electronic Tasbih Card
                AboutGuideCard(
                    title = if (isAr) "📿 السبحة الرقمية وويدجت الأذكار" else "📿 Digital Tasbih & Adhkar Widgets",
                    items = if (isAr) listOf(
                        "السبحة الإلكترونية: سبحة ذكية تفاعلية للذكر والاستغفار مع اهتزاز لمسي هادئ عند العد، وحفظ الإحصائيات مع إمكانية التصفير بسهولة.",
                        "ويدجت الأذكار للشاشة الرئيسية: ويدجت مستقلة لأذكار الصباح وأذكار المساء لتلاوة الأذكار اليومية مباشرة من شاشة هاتفك."
                    ) else listOf(
                        "Digital Tasbih: Smart interactive counter for tasbih, dhikr, and forgiveness with subtle haptic vibration, count persistence, and easy reset.",
                        "Home Screen Adhkar Widgets: Dedicated widgets for Morning and Evening Adhkar to recite daily supplications right from your home screen."
                    ),
                    isAr = isAr
                )

                // 5. Notifications & Widgets Card
                AboutGuideCard(
                    title = if (isAr) "🔔 لوحة الإشعارات والويدجت" else "🔔 Notifications & Home Widgets",
                    items = if (isAr) listOf(
                        "إشعار الصلاة الدائم: إشعار ثابت في لوحة الإشعارات وشاشة القفل يعرض الصلاة القادمة، عداداً تنازلياً حياً بالثواني، والوقت المنقضي على الصلاة السابقة.",
                        "ويدجت الشاشة الرئيسية المتنوعة: 5 تصميمات ويدجت مختلفة (المواقيت اليومية والإمساك، الصلاة القادمة، المصغر الفاخر، العمودي، وأذكار الصباح والمساء)."
                    ) else listOf(
                        "Persistent Notification: Live lockscreen & notification shade card with next prayer, live countdown, and elapsed prayer timer.",
                        "Versatile Home Widgets: 5 distinct home screen widgets (Daily Times & Imsak, Next Prayer, Compact Premium, Vertical Clock, and Morning/Evening Adhkar)."
                    ),
                    isAr = isAr
                )

                // 6. Customization Card
                AboutGuideCard(
                    title = if (isAr) "🎨 التخصيص والمظهر" else "🎨 Customization & Appearance",
                    items = if (isAr) listOf(
                        "المظهر الداكن والفاتح: دعم كامل للوضع الليلي المريح للعين أو المزامنة مع وضع النظام، مع باقات ألوان وثيمات مميزة.",
                        "خلفيات المساجد المتغيرة: خلفيات حية لمعالم ومساجد تاريخية (المسجد الحرام، المسجد النبوي، المسجد الأقصى، جامع الجزائر، وجامع الشيخ زايد) تتغير تلقائياً مع حلول كل صلاة.",
                        "نظام الأرقام وتنسيق الوقت: حرية الاختيار بين الأرقام العربية/الغربية (123) أو الأرقام الشرقية/الهندية (١٢٣)، والتبديل بين نظام 12 ساعة ونظام 24 ساعة."
                    ) else listOf(
                        "Dark & Light Themes: Eye-friendly dark mode, system-sync options, and rich color palettes.",
                        "Dynamic Mosque Backgrounds: Beautiful historic mosque wallpapers (Makkah, Madinah, Al-Aqsa, Great Mosque of Algiers, Sheikh Zayed) changing automatically with each prayer.",
                        "Numerals & Time Format: Flexible choice between Western (123) and Eastern Arabic (١٢٣) digits, and 12-hour or 24-hour time."
                    ),
                    isAr = isAr
                )

                // 7. Privacy & Developer Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAr) "الخصوصية والتواصل مع المطور" else "Privacy & Contact",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = if (isAr)
                                "نلتزم بحماية خصوصيتك؛ فالتطبيق لا يجمع أي بيانات شخصية ويعمل محلياً.\nلأي استفسار، اقتراح أو ملاحظة، يرجى التواصل عبر البريد:\nCherifsouigat@gmail.com"
                            else
                                "Your privacy is protected. The app processes data locally without collecting personal info.\nFor inquiries, suggestions, or feedback, contact:\nCherifsouigat@gmail.com",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Done Button
                Button(
                    onClick = { activeSubDialog = null },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isAr) "تم" else "Done",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }

    // 9. Rate SubDialog
    if (activeSubDialog == "rate") {
        SettingsSubDialogContainer(
            title = if (isAr) "تقييم التطبيق" else "Rate the App",
            icon = Icons.Default.Star,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (isAr) "رأيك يهمنا لمواصلة تطوير تطبيق Adhan+" else "Your feedback helps us continuously improve the app",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isAr) "يرجى اختيار تقييمك للتطبيق:" else "Please select your rating:",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var currentRating by remember { mutableStateOf(5) }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { currentRating = star },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = if (isAr) "$star نجوم" else "$star stars",
                                tint = if (star <= currentRating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        try {
                            val uri = Uri.parse("market://details?id=${context.packageName}")
                            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(goToMarket)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, if (isAr) "شكراً جزيلاً على تقييمك ودعمك! ⭐" else "Thank you for your rating and support! ⭐", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        activeSubDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isAr) "إرسال التقييم" else "Submit Rating", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 10. Privacy SubDialog
    if (activeSubDialog == "privacy") {
        SettingsSubDialogContainer(
            title = if (isAr) "شروط الاستخدام والخصوصية" else "Terms & Privacy Policy",
            icon = Icons.Default.Book,
            onDismiss = { activeSubDialog = null }
        ) {
            Text(
                text = if (isAr) "جميع الحقوق محفوظة لـ ‎Adhan+. نلتزم بحماية خصوصية بياناتك وموقعك الجغرافي وعدم مشاركتها مع أي أطراف خارجية."
                       else "All rights reserved for ‎Adhan+. We are committed to safeguarding your personal data and geographical location, and will never share them with third parties.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }

    // 11. Storage Location SubDialog
    if (activeSubDialog == "storage_location") {
        SettingsSubDialogContainer(
            title = if (isAr) "مكان التخزين وتنزيل السور" else "Storage Location & Surah Downloads",
            icon = Icons.Default.FolderOpen,
            onDismiss = { activeSubDialog = null }
        ) {
            val hasSdCard = remember(context) { com.example.util.QuranStorageManager.hasSdCard(context) }
            val internalFreeBytes = remember(context) { com.example.util.QuranStorageManager.getStorageFreeSpace(context, false) }
            val internalTotalBytes = remember(context) { com.example.util.QuranStorageManager.getStorageTotalSpace(context, false) }
            val sdFreeBytes = remember(context, hasSdCard) {
                if (hasSdCard) com.example.util.QuranStorageManager.getStorageFreeSpace(context, true) else 0L
            }
            val sdTotalBytes = remember(context, hasSdCard) {
                if (hasSdCard) com.example.util.QuranStorageManager.getStorageTotalSpace(context, true) else 0L
            }

            fun formatBytes(bytes: Long): String {
                val gb = bytes.toDouble() / (1024 * 1024 * 1024)
                return if (gb >= 1.0) {
                    if (isAr) String.format(java.util.Locale.US, "%.1f جيجابايت", gb)
                    else String.format(java.util.Locale.US, "%.1f GB", gb)
                } else {
                    val mb = bytes / (1024 * 1024)
                    if (isAr) "$mb ميجابايت" else "$mb MB"
                }
            }

            val internalFreeStr = remember(internalFreeBytes, isAr) { formatBytes(internalFreeBytes) }
            val internalTotalStr = remember(internalTotalBytes, isAr) { formatBytes(internalTotalBytes) }
            val sdFreeStr = remember(sdFreeBytes, isAr) { formatBytes(sdFreeBytes) }
            val sdTotalStr = remember(sdTotalBytes, isAr) { formatBytes(sdTotalBytes) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isAr) "اختر وحدة التخزين لحفظ التلاوات الصوتية المحملة:" else "Choose where to save downloaded audio recitations:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Option 1: Internal Storage
                val isInternalSelected = !useSdCardForDownloads
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInternalSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        width = if (isInternalSelected) 1.5.dp else 1.dp,
                        color = if (isInternalSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetUseSdCardForDownloads(false) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isInternalSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.5.dp, if (isInternalSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isInternalSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) "ذاكرة الهاتف الداخلية" else "Internal phone storage",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInternalSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAr) "المساحة المتاحة: $internalFreeStr من أصل $internalTotalStr" else "$internalFreeStr available of $internalTotalStr",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: SD Card
                val isSdSelected = useSdCardForDownloads
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSdSelected && hasSdCard)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else if (!hasSdCard)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        width = if (isSdSelected && hasSdCard) 1.5.dp else 1.dp,
                        color = if (isSdSelected && hasSdCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = hasSdCard) {
                            onSetUseSdCardForDownloads(true)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSdSelected && hasSdCard) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.5.dp, if (isSdSelected && hasSdCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSdSelected && hasSdCard) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasSdCard) {
                                    if (isAr) "بطاقة الذاكرة الخارجية (SD Card)" else "External memory card (SD card)"
                                } else {
                                    if (isAr) "بطاقة الذاكرة (غير مدرجة)" else "External memory card (Not inserted)"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!hasSdCard) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else if (isSdSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (hasSdCard) {
                                Text(
                                    text = if (isAr) "المساحة المتاحة: $sdFreeStr من أصل $sdTotalStr" else "$sdFreeStr available of $sdTotalStr",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = if (isAr) "الرجاء إدخال بطاقة SD لتفعيل هذا الخيار" else "Please insert an SD card to enable this option",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Current Path Info Card
                val currentPath = remember(useSdCardForDownloads) {
                    com.example.util.QuranStorageManager.getAudioDirectory(context, useSdCardForDownloads)?.absolutePath ?: (if (isAr) "تعذر تحديد المسار" else "Unable to determine path")
                }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (isAr) "مسار الحفظ المعتمد للتلاوات:" else "Current save path:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentPath,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Information Note
                Text(
                    text = if (isAr) "💡 ملاحظة: تُحفظ التلاوات الصوتية المحملة في وحدة التخزين المحددة لتوفير سعة الهاتف، بينما تبقى نصوص السور وقاعدة بيانات التطبيق في الذاكرة الداخلية فائقة السرعة لضمان أفضل وأسرع أداء."
                           else "💡 Note: Downloaded audio recitations are saved to the selected storage location to save device capacity, while Quran text and the application database remain in high-speed internal storage to ensure optimal and responsive performance.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = if (isAr) "تنزيل المحتوى دفعة واحدة (للاستخدام دون إنترنت):" else "Bulk downloads for offline use:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Download Quran Texts Button
                Button(
                    onClick = onDownloadAllTexts,
                    enabled = !isDownloadingAllTexts && downloadingSurahNumber == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) {
                            if (isDownloadingAllTexts) "جاري تحميل نصوص المصحف..." else "تحميل جميع نصوص المصحف الشريف"
                        } else {
                            if (isDownloadingAllTexts) "Downloading Quran texts..." else "Download All Quran Texts"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Download Reciter Audio Button
                Button(
                    onClick = onDownloadAllAudio,
                    enabled = !isDownloadingAllTexts && downloadingSurahNumber == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) {
                            if (downloadingSurahNumber == -1) "جاري تحميل المصحف الصوتي كاملاً..." else "تحميل المصحف الصوتي كاملاً (للقارئ الحالي)"
                        } else {
                            if (downloadingSurahNumber == -1) "Downloading full audio Quran..." else "Download Full Audio Quran (Current Reciter)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (downloadStatus != null) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = downloadStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (downloadProgress > 0f) {
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 12. Numeral System SubDialog
    if (activeSubDialog == "numeral_system") {
        SettingsSubDialogContainer(
            title = if (isAr) "نظام الأرقام" else "Number System",
            icon = Icons.Default.FormatSize,
            onDismiss = { activeSubDialog = null }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isAr) "اختر رسم ونظام كتابة الأرقام في جميع شاشات التطبيق والمواقيت والمصحف:" else "Choose the numeral system across all app screens, prayer times, and the Quran:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Option 1: Western Arabic (Ghubari: 1, 2, 3)
                val isWestern = numeralSystem != "eastern"
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWestern)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        width = if (isWestern) 1.5.dp else 1.dp,
                        color = if (isWestern) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNumeralSystem("western") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isWestern) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.5.dp, if (isWestern) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isWestern) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) "الأرقام العربية (الغبارية)" else "Western Arabic numerals (1, 2, 3)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWestern) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "1, 2, 3, 4, 5, 6, 7, 8, 9, 0",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isAr) "الرسم الشائع والمعتمد في المغرب العربي وعالمياً" else "Standard numerals used in North Africa and globally",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Eastern Arabic (Indic: ١، ٢، ٣)
                val isEastern = numeralSystem == "eastern"
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEastern)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        width = if (isEastern) 1.5.dp else 1.dp,
                        color = if (isEastern) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNumeralSystem("eastern") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isEastern) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.5.dp, if (isEastern) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isEastern) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) "الأرقام المشرقية (الهندية)" else "Eastern Arabic numerals (١، ٢، ٣)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEastern) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "١، ٢، ٣، ٤، ٥، ٦، ٧، ٨، ٩، ٠",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isAr) "الرسم المعتمد في المشرق العربي ومصاحف المدينة المنورة" else "Official standard used in the Middle East and Madinah Mushafs",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (manualUpdateDialogInfo != null) {
            com.example.ui.components.AppUpdateDialog(
                manifest = manualUpdateDialogInfo!!,
                installedVersionCode = installedVersionCode,
                installedVersionName = installedVersionName,
                isAr = isAr,
                onDismiss = { manualUpdateDialogInfo = null }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    sectionTitle: String,
    rows: List<SettingRowData>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = sectionTitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                rows.forEachIndexed { index, rowData ->
                    SettingRowItem(rowData = rowData)
                    if (index < rows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRowItem(
    rowData: SettingRowData
) {
    val clickableModifier = if (rowData.onClick != null) {
        Modifier.clickable { rowData.onClick.invoke() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .then(clickableModifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Icon in 40dp shaded circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rowData.icon,
                    contentDescription = rowData.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = rowData.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (rowData.subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rowData.subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Trailing Content (Switch, Segmented buttons, or Chevron)
        if (rowData.trailingContent != null) {
            rowData.trailingContent.invoke()
        } else if (rowData.onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AboutGuideCard(
    title: String,
    items: List<String>,
    isAr: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSubDialogContainer(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val isAr = LocalLayoutDirection.current == LayoutDirection.Rtl

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides (if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr),
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = CairoFontFamily)
        ) {
            ProvideTextStyle(value = TextStyle(fontFamily = CairoFontFamily)) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 480.dp)
                        .padding(vertical = 16.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SubDialog Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "رجوع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerToggleRow(
    prayerNameAr: String,
    prayerNameEn: String = "",
    emoji: String,
    isEnabled: Boolean,
    isAr: Boolean = true,
    onToggle: (Boolean) -> Unit
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
            Text(text = emoji, fontSize = 16.sp)
            Text(
                text = if (isAr) "أذان $prayerNameAr" else "$prayerNameEn Adhan",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
