package com.example.ui


import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PRESET_BACKGROUND_THEMES
import com.example.data.model.PrayerOffsets
import com.example.ui.components.AdhanSettingsDialog
import com.example.ui.components.AppNavbar
import com.example.ui.components.LocationDialog
import com.example.ui.screens.HolidaysScreen
import com.example.ui.screens.PrayerClockScreen
import com.example.ui.screens.PrayersListScreen
import com.example.ui.screens.PrayersScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.util.AppLocalization
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.LocalAppThemeTokens
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.getAppThemeTokens
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.launch

data class SoundHintData(
    val icon: String,
    val title: String,
    val soundName: String,
    val isEnabled: Boolean
)

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val appThemePalette by viewModel.appThemePalette.collectAsState()
    val appThemeStyle by viewModel.appThemeStyle.collectAsState()
    val followSystemDarkMode by viewModel.followSystemDarkMode.collectAsState()

    val cityName by viewModel.cityName.collectAsState()
    val countryName by viewModel.countryName.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val prayerOffsets by viewModel.prayerOffsets.collectAsState()

    val prayerTimings by viewModel.prayerTimings.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    val currentTimeText by viewModel.currentTimeText.collectAsState()
    val isPrayerLoading by viewModel.isPrayerLoading.collectAsState()
    val isRefreshingLocation by viewModel.isRefreshingLocation.collectAsState()

    val quranEdition by viewModel.quranEdition.collectAsState()
    val quranFontSize by viewModel.quranFontSize.collectAsState()
    val quranFontType by viewModel.quranFontType.collectAsState()
    val selectedReciterId by viewModel.selectedReciterId.collectAsState()
    val startAyah by viewModel.startAyah.collectAsState()
    val endAyah by viewModel.endAyah.collectAsState()
    val repeatPerAyah by viewModel.repeatPerAyah.collectAsState()
    val playingAyahNumber by viewModel.playingAyahNumber.collectAsState()
    val isPlayingQuranAudio by viewModel.isPlayingQuranAudio.collectAsState()
    val isQuranAudioPaused by viewModel.isQuranAudioPaused.collectAsState()
    val playbackProgressRatio by viewModel.playbackProgressRatio.collectAsState()
    val surahs by viewModel.allSurahs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val targetScrollAyah by viewModel.targetScrollAyah.collectAsState()
    val isQuranFullScreen by viewModel.isQuranFullScreen.collectAsState()
    val isQuranLoading by viewModel.isQuranLoading.collectAsState()
    val quranError by viewModel.quranError.collectAsState()
    val bookmark by viewModel.bookmark.collectAsState()

    val useSdCardForDownloads by viewModel.useSdCardForDownloads.collectAsState()
    val downloadingSurahNumber by viewModel.downloadingSurahNumber.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val isDownloadingAllTexts by viewModel.isDownloadingAllTexts.collectAsState()

    val prayerAdhanSettings by viewModel.prayerAdhanSettings.collectAsState()
    val useRoosterForSunrise by viewModel.useRoosterForSunrise.collectAsState()
    val overrideSilentMode by viewModel.overrideSilentMode.collectAsState()
    val useFlipToMute by viewModel.useFlipToMute.collectAsState()
    val notificationStyle by viewModel.notificationStyle.collectAsState()
    val showLockScreenPrayerNotification by viewModel.showLockScreenPrayerNotification.collectAsState()
    val calculationMethodId by viewModel.calculationMethodId.collectAsState()
    val hijriOffset by viewModel.hijriOffset.collectAsState()
    val asrMethod by viewModel.asrMethod.collectAsState()
    val fajrFormulaWithTatweeb by viewModel.fajrFormulaWithTatweeb.collectAsState()
    val appBackgroundTheme by viewModel.appBackgroundTheme.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val selectedAdhanId by viewModel.selectedAdhanId.collectAsState()
    val selectedFajrSoundId by viewModel.selectedFajrAdhanId.collectAsState()
    val customSoundUri by viewModel.customSoundUri.collectAsState()
    val customSoundName by viewModel.customSoundName.collectAsState()
    val isAdhanPlaying by viewModel.isAdhanPlaying.collectAsState()
    val numeralSystem by viewModel.numeralSystem.collectAsState()
    val adhanAudioStreamType by viewModel.adhanAudioStreamType.collectAsState()
    val recitationNetworkError by viewModel.recitationNetworkError.collectAsState()
    val downloadedSurahs by viewModel.downloadedSurahs.collectAsState()
    val showMosqueBackgrounds by viewModel.showMosqueBackgrounds.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()

    val isAr = appLanguage != "en"
    val displayCityName = remember(cityName, appLanguage) {
        AppLocalization.getLocalizedCityName(cityName, appLanguage)
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    var showSplashScreen by remember { mutableStateOf(true) }
    var showLocationModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showCalcMethodModal by remember { mutableStateOf(false) }
    var showAdhanSoundsModal by remember { mutableStateOf(false) }
    var showHijriModal by remember { mutableStateOf(false) }
    var showAppearanceModal by remember { mutableStateOf(false) }
    var showContactUsModal by remember { mutableStateOf(false) }
    var showPrayerOffsetsModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    var showNotificationRationaleDialog by remember { mutableStateOf(false) }
    var showBatteryOptDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.resumeClockLoop()
                viewModel.checkAndAutoUpdateLocation()
                viewModel.startContinuousLocationUpdates()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                viewModel.pauseClockLoop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var soundHintData by remember { mutableStateOf<SoundHintData?>(null) }
    var hintTriggerKey by remember { mutableStateOf(0L) }

    androidx.compose.runtime.LaunchedEffect(hintTriggerKey) {
        if (soundHintData != null) {
            kotlinx.coroutines.delay(2800L)
            soundHintData = null
        }
    }

    val currentAdhanName = remember(selectedAdhanId, customSoundName, appLanguage) {
        if (selectedAdhanId == "custom_local") {
            customSoundName ?: if (isAr) "صوت مخصص" else "Custom Audio"
        } else {
            val sound = com.example.data.model.PRESET_ADHAN_SOUNDS.find { it.id == selectedAdhanId }
                ?: com.example.data.model.PRESET_ADHAN_SOUNDS.first()
            if (isAr) sound.nameAr else sound.nameEn
        }
    }

    val currentFajrAdhanName = remember(selectedFajrSoundId, customSoundName, appLanguage) {
        if (selectedFajrSoundId == "custom_local") {
            customSoundName ?: if (isAr) "صوت مخصص" else "Custom Audio"
        } else {
            val sound = com.example.data.model.PRESET_ADHAN_SOUNDS.find { it.id == selectedFajrSoundId }
                ?: com.example.data.model.PRESET_ADHAN_SOUNDS.first()
            if (isAr) sound.nameAr else sound.nameEn
        }
    }

    val onTogglePrayerSoundWithHint = remember(currentAdhanName, currentFajrAdhanName, isAr, appLanguage) {
        { prayerKey: String, enabled: Boolean ->
            viewModel.togglePrayerAdhan(prayerKey, enabled)
            val prayerName = AppLocalization.getPrayerName(prayerKey, appLanguage)
            val isFajr = prayerKey.lowercase() == "fajr"
            val soundTitle = if (isFajr) {
                if (isAr) "$currentFajrAdhanName (مع التثويب)"
                else "$currentFajrAdhanName (with Tathweeb)"
            } else {
                currentAdhanName
            }

            soundHintData = SoundHintData(
                icon = if (enabled) (if (isFajr) "🕌" else "🔊") else "🔇",
                title = if (enabled) {
                    if (isAr) "تم تفعيل أذان $prayerName" else "$prayerName Adhan: Enabled"
                } else {
                    if (isAr) "تم كتم أذان $prayerName" else "$prayerName Adhan: Muted"
                },
                soundName = if (isAr) {
                    if (enabled) "صوت الأذان: $soundTitle" else "الصوت الحالي: $soundTitle"
                } else {
                    if (enabled) "Adhan Sound: $soundTitle" else "Current Sound: $soundTitle"
                },
                isEnabled = enabled
            )
            hintTriggerKey = System.currentTimeMillis()
        }
    }

    val onToggleSunriseSoundWithHint = remember(isAr, useRoosterForSunrise) {
        { enabled: Boolean ->
            viewModel.toggleRoosterForSunrise(enabled)
            val roosterSound = if (isAr) "صوت صياح الديك الأصيل" else "Authentic Rooster Crow"
            soundHintData = SoundHintData(
                icon = if (enabled) "🐓" else "🔕",
                title = if (enabled) {
                    if (isAr) "تم تفعيل تنبيه الشروق" else "Sunrise Alert: Enabled"
                } else {
                    if (isAr) "تم كتم تنبيه الشروق" else "Sunrise Alert: Muted"
                },
                soundName = if (isAr) {
                    if (enabled) "صوت الشروق: $roosterSound" else "الصوت الحالي: $roosterSound"
                } else {
                    if (enabled) "Sunrise Sound: $roosterSound" else "Current Sound: $roosterSound"
                },
                isEnabled = enabled
            )
            hintTriggerKey = System.currentTimeMillis()
        }
    }

    // Request notification permission dynamically on Android 13+ (API 33+) if needed
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val notificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                showNotificationRationaleDialog = true
            } else {
                val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (pm != null && !pm.isIgnoringBatteryOptimizations(context.packageName)) {
                        showBatteryOptDialog = true
                    }
                }
            }
        }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val expandedSections by viewModel.expandedSections.collectAsState()

    val anyModalOpen = showLocationModal || showSettingsModal || showCalcMethodModal ||
        showAdhanSoundsModal || showHijriModal || showAppearanceModal || showContactUsModal ||
        showPrayerOffsetsModal
    val isQuranReading = currentTab == AppTab.QURAN && (selectedSurah != null || isQuranFullScreen)

    BackHandler(enabled = showSplashScreen || anyModalOpen || isQuranReading || currentTab != AppTab.PRAYER_CLOCK) {
        when {
            showSplashScreen -> showSplashScreen = false
            showLocationModal -> showLocationModal = false
            showSettingsModal -> showSettingsModal = false
            showCalcMethodModal -> showCalcMethodModal = false
            showAdhanSoundsModal -> showAdhanSoundsModal = false
            showHijriModal -> showHijriModal = false
            showAppearanceModal -> showAppearanceModal = false
            showContactUsModal -> showContactUsModal = false
            showPrayerOffsetsModal -> showPrayerOffsetsModal = false
            currentTab == AppTab.QURAN && isQuranFullScreen -> viewModel.setQuranFullScreen(false)
            currentTab == AppTab.QURAN && selectedSurah != null -> viewModel.closeSurahView()
            currentTab != AppTab.PRAYER_CLOCK -> viewModel.setTab(AppTab.PRAYER_CLOCK)
        }
    }

    val currentDensity = LocalDensity.current
    val clampedDensity = remember(currentDensity) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale.coerceIn(0.85f, 1.15f)
        )
    }

    val activity = context as? Activity
    val isImmersiveQuran = isQuranFullScreen && currentTab == AppTab.QURAN && selectedSurah != null

    DisposableEffect(isImmersiveQuran) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isImmersiveQuran) {
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.window?.let { win ->
                WindowCompat.getInsetsController(win, win.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val systemIsDark = isSystemInDarkTheme()
    val effectiveIsDark = if (followSystemDarkMode) systemIsDark else isDarkMode
    val currentTokens = getAppThemeTokens(appThemeStyle, effectiveIsDark)

    val currentTheme = PRESET_BACKGROUND_THEMES.find { it.id == appBackgroundTheme } ?: PRESET_BACKGROUND_THEMES.first()
    val fullBleedBg = if (currentTab == AppTab.PRAYER_CLOCK) currentTokens.bg else Color(if (effectiveIsDark) currentTheme.darkBgColor else currentTheme.lightBgColor)

    MyApplicationTheme(
        themePalette = appThemePalette,
        darkTheme = effectiveIsDark
    ) {
        CompositionLocalProvider(
            LocalAppThemeTokens provides currentTokens,
            LocalLayoutDirection provides (if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr),
            LocalDensity provides clampedDensity,
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = CairoFontFamily),
            com.example.ui.util.AppLocalization.LocalNumeralSystem provides numeralSystem
        ) {
            ProvideTextStyle(value = TextStyle(fontFamily = CairoFontFamily)) {
                Crossfade(targetState = showSplashScreen, label = "SplashScreenFade") { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplashScreen = false }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(fullBleedBg)
                        ) {
                                Scaffold(
                                    modifier = Modifier.fillMaxSize(),
                                    containerColor = Color.Transparent,
                                    contentWindowInsets = if (isImmersiveQuran) WindowInsets(0, 0, 0, 0) else WindowInsets.systemBars,
                                    bottomBar = {
                                        if (!isQuranFullScreen || currentTab != AppTab.QURAN || selectedSurah == null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .navigationBarsPadding()
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                AppNavbar(
                                                    currentTab = currentTab,
                                                    onTabSelected = { viewModel.setTab(it) },
                                                    onOpenSettings = { showSettingsModal = true },
                                                    onOpenDrawer = { showSettingsModal = true },
                                                    appLanguage = appLanguage
                                                )
                                            }
                                        }
                                    }
                                ) { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(if (isImmersiveQuran) PaddingValues(0.dp) else innerPadding),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        when (currentTab) {
                                                AppTab.PRAYER_CLOCK -> {
                                                    PrayerClockScreen(
                                                        cityName = displayCityName,
                                                        currentTimeText = currentTimeText,
                                                        prayerTimings = prayerTimings,
                                                        nextPrayer = nextPrayer,
                                                        isDarkMode = effectiveIsDark,
                                                        backgroundThemeId = appBackgroundTheme,
                                                        showMosqueBackgrounds = showMosqueBackgrounds,
                                                        onOpenDrawer = { showSettingsModal = true },
                                                        onOpenSettings = { showSettingsModal = true },
                                                        onOpenLocationDialog = { showLocationModal = true },
                                                        onOpenNearestMosque = { viewModel.openNearestMosque(context) },
                                                        prayerAdhanSettings = prayerAdhanSettings,
                                                        useRoosterForSunrise = useRoosterForSunrise,
                                                        onToggleRoosterForSunrise = { enabled ->
                                                            onToggleSunriseSoundWithHint(enabled)
                                                        },
                                                        onTogglePrayerAdhan = { prayerKey, enabled ->
                                                            onTogglePrayerSoundWithHint(prayerKey, enabled)
                                                        },
                                                        onCycleTheme = { viewModel.cycleNextAppTheme() },
                                                        appLanguage = appLanguage,
                                                        isRefreshing = isRefreshingLocation,
                                                        onRefresh = { viewModel.refreshLocationAndPrayers() }
                                                    )
                                                }
                                                AppTab.PRAYER_LIST -> {
                                                    PrayersListScreen(
                                                        cityName = displayCityName,
                                                        hijriDate = hijriDate,
                                                        prayerTimings = prayerTimings,
                                                        nextPrayer = nextPrayer,
                                                        prayerAdhanSettings = prayerAdhanSettings,
                                                        useRoosterForSunrise = useRoosterForSunrise,
                                                        onToggleRoosterForSunrise = { enabled ->
                                                            onToggleSunriseSoundWithHint(enabled)
                                                        },
                                                        onTogglePrayerAdhan = { prayerKey, enabled ->
                                                            onTogglePrayerSoundWithHint(prayerKey, enabled)
                                                        },
                                                        isDarkMode = isDarkMode,
                                                        backgroundThemeId = appBackgroundTheme,
                                                        onOpenSettings = { showSettingsModal = true },
                                                        onOpenLocationDialog = { showLocationModal = true },
                                                        onOpenNearestMosque = { viewModel.openNearestMosque(context) },
                                                        onCycleBackgroundTheme = { viewModel.cycleNextAppTheme() },
                                                 appLanguage = appLanguage,
                                                        latitude = latitude,
                                                        longitude = longitude,
                                                        calculationMethodId = calculationMethodId,
                                                        prayerOffsets = prayerOffsets,
                                                        hijriOffset = hijriOffset,
                                                        asrMethod = asrMethod,
                                                        showMosqueBackgrounds = showMosqueBackgrounds,
                                                        isRefreshing = isRefreshingLocation,
                                                        onRefresh = { viewModel.refreshLocationAndPrayers() }
                                                    )
                                                }
                                                AppTab.QIBLA -> {
                                                    QiblaScreen(
                                                        userLat = latitude,
                                                        userLng = longitude,
                                                        cityName = displayCityName,
                                                        onLocationUpdate = { newLat, newLng, newCity ->
                                                            viewModel.updateLocation(newCity, countryName, newLat, newLng)
                                                        },
                                                        onOpenSettings = { showSettingsModal = true },
                                                        appLanguage = appLanguage,
                                                        isDarkMode = isDarkMode,
                                                        backgroundThemeId = appBackgroundTheme
                                                    )
                                                }
                                                AppTab.HOLIDAYS -> {
                                                    HolidaysScreen(
                                                        hijriDate = hijriDate,
                                                        isDarkMode = isDarkMode,
                                                        onOpenSettings = { showSettingsModal = true },
                                                        appLanguage = appLanguage
                                                    )
                                                }
                                                AppTab.QURAN -> {
                                                    QuranScreen(
                                                        quranEdition = quranEdition,
                                                        quranFontSize = quranFontSize,
                                                        quranFontType = quranFontType,
                                                        selectedReciterId = selectedReciterId,
                                                        startAyah = startAyah,
                                                        endAyah = endAyah,
                                                        repeatPerAyah = repeatPerAyah,
                                                        playingAyahNumber = playingAyahNumber,
                                                        isPlayingQuranAudio = isPlayingQuranAudio,
                                                        isQuranAudioPaused = isQuranAudioPaused,
                                                        playbackProgressRatio = playbackProgressRatio,
                                                        surahs = surahs,
                                                        searchQuery = searchQuery,
                                                        selectedSurah = selectedSurah,
                                                        targetScrollAyah = targetScrollAyah,
                                                        onClearTargetScrollAyah = { viewModel.clearTargetScrollAyah() },
                                                        bookmark = bookmark,
                                                        isLoading = isQuranLoading,
                                                        errorMessage = quranError,
                                                        onEditionChanged = { viewModel.setQuranEdition(it) },
                                                        onFontSizeChanged = { viewModel.updateQuranFontSize(it) },
                                                        onFontTypeChanged = { viewModel.updateQuranFontType(it) },
                                                        onReciterChanged = { viewModel.setReciterId(it) },
                                                        onRangeChanged = { start, end -> viewModel.setAyahRange(start, end) },
                                                        onRepeatCountChanged = { viewModel.setRepeatPerAyah(it) },
                                                        onTogglePlayAudio = { viewModel.togglePlayQuranAudio(it) },
                                                        onPauseAudio = { viewModel.pauseQuranAudio() },
                                                        onResumeAudio = { viewModel.resumeQuranAudio() },
                                                        onStopAudio = { viewModel.stopQuranAudio() },
                                                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                                        onSurahSelected = { surahNum, targetAyah -> viewModel.openSurah(surahNum, targetAyah) },
                                                        onCloseSurahView = { viewModel.closeSurahView() },
                                                        onSaveBookmark = { surahNum, surahName, ayahNum ->
                                                            viewModel.saveBookmark(surahNum, surahName, ayahNum)
                                                        },
                                                        useSdCardForDownloads = useSdCardForDownloads,
                                                        downloadingSurahNumber = downloadingSurahNumber,
                                                        downloadProgress = downloadProgress,
                                                        downloadStatus = downloadStatus,
                                                        isDownloadingAllTexts = isDownloadingAllTexts,
                                                        isFullScreen = isQuranFullScreen,
                                                        onToggleFullScreen = { viewModel.toggleQuranFullScreen() },
                                                        onSetUseSdCardForDownloads = { viewModel.setUseSdCardForDownloads(it) },
                                                        onDownloadSurah = { num, reciter, total -> viewModel.downloadSurah(num, reciter, total) },
                                                 onDownloadAllTexts = { viewModel.downloadAllQuranTexts() },

                                                        recitationNetworkError = recitationNetworkError,
                                                        onDismissRecitationError = { viewModel.dismissRecitationError() },
                                                        downloadedSurahs = downloadedSurahs,
                                                        appLanguage = appLanguage
                                                    )
                                                }
                                            }

                                        // In-App Adhan Alert Banner (High-Contrast Radiant Emerald & Gold)
                                        AnimatedVisibility(
                                            visible = isAdhanPlaying,
                                            enter = fadeIn() + slideInVertically(),
                                            exit = fadeOut() + slideOutVertically(),
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                                .zIndex(99f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(22.dp),
                                                color = Color(0xFF064E3B),
                                                border = BorderStroke(2.dp, Color(0xFFFDE047)),
                                                shadowElevation = 12.dp,
                                                modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(42.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFFDE047)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.VolumeUp,
                                                                contentDescription = null,
                                                                tint = Color(0xFF064E3B),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                        Column {
                                                            val adhanPrayerName = nextPrayer?.nameAr ?: "الصلاة"
                                                            Text(
                                                                text = "حان الآن موعد أذان $adhanPrayerName",
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.Black,
                                                                color = Color.White,
                                                                fontFamily = CairoFontFamily
                                                            )
                                                            Text(
                                                                text = "حي على الصلاة • حي على الفلاح",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFFEF08A),
                                                                fontFamily = CairoFontFamily
                                                            )
                                                        }
                                                    }

                                                    Button(
                                                        onClick = { viewModel.stopAdhanSound() },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFDC2626),
                                                            contentColor = Color.White
                                                        ),
                                                        shape = RoundedCornerShape(14.dp),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
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
                                                            fontFamily = CairoFontFamily,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Dialogs moved inside the Box
                                        if (showSettingsModal) {
                                            com.example.ui.components.FullSettingsDialog(
                                                appLanguage = appLanguage,
                                                onSelectLanguage = { viewModel.setAppLanguage(it) },
                                                notificationStyle = notificationStyle,
                                                onSelectNotificationStyle = { viewModel.setNotificationStyle(it) },
                                                calculationMethodId = calculationMethodId,
                                                onSelectCalculationMethod = { newMethod ->
                                                    viewModel.setCalculationMethod(newMethod)
                                                },
                                                asrMethod = asrMethod,
                                                onSelectAsrMethod = { viewModel.setAsrMethod(it) },
                                                fajrFormulaWithTatweeb = fajrFormulaWithTatweeb,
                                                onToggleFajrFormulaWithTatweeb = { viewModel.toggleFajrFormulaWithTatweeb(it) },
                                                prayerAdhanSettings = prayerAdhanSettings,
                                                onTogglePrayerAdhan = { prayerKey, enabled ->
                                                    viewModel.togglePrayerAdhan(prayerKey, enabled)
                                                },
                                                onToggle24HourFormat = { viewModel.toggle24HourFormat(it) },
                                                prayerOffsets = prayerOffsets,
                                                onUpdateOffsets = { viewModel.updateOffsets(it) },
                                                selectedSoundId = selectedAdhanId,
                                                customSoundUri = customSoundUri,
                                                customSoundName = customSoundName,
                                                onSaveAdhanSound = { soundId, newUri, newName ->
                                                    viewModel.updateAdhanSound(soundId, selectedFajrSoundId, newUri, newName)
                                                },
                                                cityName = cityName,
                                                countryName = countryName,
                                                onOpenLocationDialog = {
                                                    showSettingsModal = false
                                                    showLocationModal = true
                                                },
                                                onFindNearestMosque = {
                                                    showSettingsModal = false
                                                    viewModel.openNearestMosque(context)
                                                },
                                                onOpenPrayerOffsets = {
                                                    showSettingsModal = false
                                                    showPrayerOffsetsModal = true
                                                },
                                                onOpenContactUs = {
                                                    showSettingsModal = false
                                                    showContactUsModal = true
                                                },
                                                hijriDate = hijriDate,
                                                hijriOffset = hijriOffset,
                                                onUpdateHijriOffset = { newOffset ->
                                                    viewModel.updateHijriOffset(newOffset)
                                                },
                                                isDarkMode = effectiveIsDark,
                                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                                appThemeStyle = appThemeStyle,
                                                onSelectThemeStyle = { viewModel.setAppThemeStyle(it) },
                                                followSystemDarkMode = followSystemDarkMode,
                                                onToggleFollowSystemDarkMode = { viewModel.setFollowSystemDarkMode(it) },
                                                appThemePalette = appThemePalette,
                                                onSelectPalette = { viewModel.setAppThemePalette(it) },
                                                appBackgroundTheme = appBackgroundTheme,
                                                onSelectBackgroundTheme = { viewModel.setAppBackgroundTheme(it) },
                                                isMuted = isMuted,
                                                onToggleMute = { viewModel.toggleMute() },
                                                overrideSilentMode = overrideSilentMode,
                                                onToggleOverrideSilentMode = { viewModel.toggleOverrideSilentMode(it) },
                                                useFlipToMute = useFlipToMute,
                                                onToggleFlipToMute = { viewModel.toggleUseFlipToMute(it) },
                                                adhanAudioStreamType = adhanAudioStreamType,
                                                onSetAdhanAudioStreamType = { viewModel.setAdhanAudioStreamType(it) },
                                                useSdCardForDownloads = useSdCardForDownloads,
                                                onSetUseSdCardForDownloads = { viewModel.setUseSdCardForDownloads(it) },
                                                numeralSystem = numeralSystem,
                                                onSelectNumeralSystem = { viewModel.setNumeralSystem(it) },
                                                isDownloadingAllTexts = isDownloadingAllTexts,
                                                downloadProgress = downloadProgress,
                                                downloadStatus = downloadStatus,
                                                onDownloadAllTexts = { viewModel.downloadAllQuranTexts() },
                                                showLockScreenPrayerNotification = showLockScreenPrayerNotification,
                                                onToggleLockScreenPrayerNotification = { viewModel.toggleLockScreenPrayerNotification(it) },
                                                showMosqueBackgrounds = showMosqueBackgrounds,
                                                onToggleMosqueBackgrounds = { viewModel.setShowMosqueBackgrounds(it) },
                                                onDismiss = { showSettingsModal = false }
                                            )
                                        }
                                        if (showLocationModal) {
                                            LocationDialog(
                                                currentCity = cityName,
                                                currentCountry = countryName,
                                                currentLat = latitude,
                                                currentLng = longitude,
                                                currentOffsets = prayerOffsets,
                                                appLanguage = appLanguage,
                                                onDismiss = { showLocationModal = false },
                                                onSave = { newCity, newCountry, newLat, newLng, newOffsets ->
                                                    viewModel.updateLocation(newCity, newCountry, newLat, newLng)
                                                    viewModel.updateOffsets(newOffsets)
                                                    showLocationModal = false
                                                }
                                            )
                                        }
                                        if (showCalcMethodModal) {
                                            com.example.ui.components.CalculationMethodDialog(
                                                selectedMethodId = calculationMethodId,
                                                selectedAsrMethod = asrMethod,
                                                appLanguage = appLanguage,
                                                onDismiss = { showCalcMethodModal = false },
                                                onSelectMethod = { newMethod ->
                                                    viewModel.setCalculationMethod(newMethod)
                                                    showCalcMethodModal = false
                                                },
                                                onSelectAsrMethod = { newAsr ->
                                                    viewModel.setAsrMethod(newAsr)
                                                }
                                            )
                                        }
                                        if (showAdhanSoundsModal) {
                                            AdhanSettingsDialog(
                                                selectedSoundId = selectedAdhanId,
                                                selectedFajrSoundId = selectedFajrSoundId,
                                                customSoundUri = customSoundUri,
                                                customSoundName = customSoundName,
                                                overrideSilentMode = overrideSilentMode,
                                                onToggleOverrideSilentMode = { viewModel.toggleOverrideSilentMode(it) },
                                                useFlipToMute = useFlipToMute,
                                                onToggleFlipToMute = { viewModel.toggleUseFlipToMute(it) },
                                                adhanAudioStreamType = adhanAudioStreamType,
                                                onSetAdhanAudioStreamType = { viewModel.setAdhanAudioStreamType(it) },
                                                appLanguage = appLanguage,
                                                onDismiss = { showAdhanSoundsModal = false },
                                                onSaveSound = { soundId, fajrSoundId, newUri, newName ->
                                                    viewModel.updateAdhanSound(soundId, fajrSoundId, newUri, newName)
                                                    showAdhanSoundsModal = false
                                                }
                                            )
                                        }

                                        // In-App Update Alert Dialog
                                        if (availableUpdate?.isUpdateAvailable == true && availableUpdate?.manifest != null) {
                                            val updateResult = availableUpdate!!
                                            val manifest = updateResult.manifest!!
                                            com.example.ui.components.AppUpdateDialog(
                                                manifest = manifest,
                                                installedVersionCode = updateResult.installedVersionCode,
                                                installedVersionName = updateResult.installedVersionName,
                                                isAr = isAr,
                                                onDismiss = { viewModel.dismissUpdateNotice() }
                                            )
                                        }
                                        if (showHijriModal) {
                                            com.example.ui.components.HijriAdjustmentDialog(
                                                currentHijriDate = hijriDate,
                                                currentOffset = hijriOffset,
                                                appLanguage = appLanguage,
                                                onDismiss = { showHijriModal = false },
                                                onSaveOffset = { newOffset ->
                                                    viewModel.updateHijriOffset(newOffset)
                                                    showHijriModal = false
                                                }
                                            )
                                        }
                                        if (showAppearanceModal) {
                                            com.example.ui.components.AppearanceSettingsDialog(
                                                isDarkMode = isDarkMode,
                                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                                appThemePalette = appThemePalette,
                                                onSelectPalette = { viewModel.setAppThemePalette(it) },
                                                appBackgroundTheme = appBackgroundTheme,
                                                onSelectBackgroundTheme = { viewModel.setAppBackgroundTheme(it) },
                                                appLanguage = appLanguage,
                                                onDismiss = { showAppearanceModal = false }
                                            )
                                        }
                                        if (showContactUsModal) {
                                            com.example.ui.components.ContactUsDialog(
                                                appLanguage = appLanguage,
                                                onDismiss = { showContactUsModal = false }
                                            )
                                        }
                                        if (showPrayerOffsetsModal) {
                                            com.example.ui.components.PrayerOffsetsDialog(
                                                currentOffsets = prayerOffsets,
                                                appLanguage = appLanguage,
                                                onDismiss = { showPrayerOffsetsModal = false },
                                                onSaveOffsets = { newOffsets ->
                                                    viewModel.updateOffsets(newOffsets)
                                                    showPrayerOffsetsModal = false
                                                }
                                            )
                                        }
                                        if (showNotificationRationaleDialog) {
                                             androidx.compose.material3.AlertDialog(
                                                 onDismissRequest = { showNotificationRationaleDialog = false },
                                                 title = { androidx.compose.material3.Text("إذن الإشعارات مطلوب") },
                                                 text = { androidx.compose.material3.Text("لضمان ظهور تنبيهات الأذان في موعدها، يرجى السماح بتلقي الإشعارات من إعدادات التطبيق.") },
                                                 confirmButton = {
                                                     androidx.compose.material3.TextButton(
                                                         onClick = {
                                                             showNotificationRationaleDialog = false
                                                             try {
                                                                 val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                                     putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                                 }
                                                                 context.startActivity(intent)
                                                             } catch (e: Exception) {
                                                                 try {
                                                                     val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                                                     context.startActivity(intent)
                                                                 } catch (_: Exception) {}
                                                             }
                                                         }
                                                     ) {
                                                         androidx.compose.material3.Text("فتح الإعدادات")
                                                     }
                                                 },
                                                 dismissButton = {
                                                     androidx.compose.material3.TextButton(onClick = { showNotificationRationaleDialog = false }) {
                                                         androidx.compose.material3.Text("لاحقاً")
                                                     }
                                                 }
                                             )
                                         }
                                         if (showBatteryOptDialog) {
                                             androidx.compose.material3.AlertDialog(
                                                 onDismissRequest = { showBatteryOptDialog = false },
                                                 title = { androidx.compose.material3.Text("تحسين استهلاك البطارية") },
                                                 text = { androidx.compose.material3.Text("لضمان عدم إيقاف تطبيق الأذان في الخلفية من قبل النظام، يرجى استثناء التطبيق من تحسينات البطارية.") },
                                                 confirmButton = {
                                                     androidx.compose.material3.TextButton(
                                                         onClick = {
                                                             showBatteryOptDialog = false
                                                             try {
                                                                 val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                                     data = android.net.Uri.parse("package:${context.packageName}")
                                                                 }
                                                                 context.startActivity(intent)
                                                             } catch (e: Exception) {
                                                                 try {
                                                                     val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                                     context.startActivity(intent)
                                                                 } catch (_: Exception) {}
                                                             }
                                                         }
                                                     ) {
                                                         androidx.compose.material3.Text("السماح")
                                                     }
                                                 },
                                                 dismissButton = {
                                                     androidx.compose.material3.TextButton(onClick = { showBatteryOptDialog = false }) {
                                                         androidx.compose.material3.Text("إلغاء")
                                                     }
                                                 }
                                             )
                                         }

                                    }

                            }

                            // Animated Floating Sound Hint Pill (High Z-Index, Safe from status bar)
                            androidx.compose.animation.AnimatedVisibility(
                                visible = soundHintData != null,
                                enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(240)) +
                                        androidx.compose.animation.slideInVertically(
                                            androidx.compose.animation.core.spring(
                                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                            )
                                        ) { -it },
                                exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) +
                                        androidx.compose.animation.slideOutVertically(androidx.compose.animation.core.tween(250)) { -it },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding()
                                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                                    .zIndex(99999f)
                            ) {
                                soundHintData?.let { hint ->
                                    androidx.compose.material3.Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF0F172A).copy(alpha = 0.96f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.4.dp,
                                            if (hint.isEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                                        ),
                                        shadowElevation = 14.dp,
                                        modifier = Modifier
                                            .clickable { soundHintData = null }
                                            .testTag("sound_hint_pill")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (hint.isEnabled) Color(0xFF10B981).copy(alpha = 0.22f)
                                                        else Color(0xFFF59E0B).copy(alpha = 0.22f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.material3.Text(
                                                    text = hint.icon,
                                                    fontSize = 19.sp
                                                )
                                            }
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(7.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (hint.isEnabled) Color(0xFF10B981)
                                                                else Color(0xFFF59E0B)
                                                            )
                                                    )
                                                    androidx.compose.material3.Text(
                                                        text = hint.title,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = CairoFontFamily
                                                    )
                                                }
                                                androidx.compose.material3.Text(
                                                    text = hint.soundName,
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    fontFamily = CairoFontFamily,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
