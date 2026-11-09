package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PRESET_ADHAN_SOUNDS
import com.example.data.model.PrayerAdhanSettings
import com.example.data.model.PrayerOffsets
import com.example.data.model.PrayerTimings
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import com.example.data.remote.AladhanApiService
import com.example.data.remote.AlquranApiService
import com.example.data.repository.PrayerRepository
import com.example.data.repository.QuranRepository
import com.example.util.AdhanAudioPlayer
import com.example.util.AdhanScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.R

enum class AppTab {
    PRAYER_CLOCK,
    PRAYER_LIST,
    QIBLA,
    HOLIDAYS,
    QURAN
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val db = AppDatabase.getDatabase(application)
    private val prayerRepo = PrayerRepository(
        AladhanApiService.create(),
        db.prayerCacheDao()
    )
    private val quranRepo = QuranRepository(
        application.applicationContext,
        AlquranApiService.create(),
        db.bookmarkDao(),
        db.favoriteSurahDao(),
        db.quranCacheDao()
    )

    // Drawer Expansion State
    private val _expandedSections = MutableStateFlow<Set<String>>(emptySet())
    val expandedSections: StateFlow<Set<String>> = _expandedSections.asStateFlow()

    fun toggleSection(sectionId: String) {
        val current = _expandedSections.value
        _expandedSections.value = if (current.contains(sectionId)) {
            current - sectionId
        } else {
            current + sectionId
        }
    }

    // Navigation & Appearance
    private val _currentTab = MutableStateFlow(AppTab.PRAYER_CLOCK)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(
        prefs.getBoolean("is_dark_mode", false)
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val initialStyle = prefs.getString("app_theme_style", "emerald") ?: "emerald"
    private val _appThemeStyle = MutableStateFlow(initialStyle)
    val appThemeStyle: StateFlow<String> = _appThemeStyle.asStateFlow()

    private val _followSystemDarkMode = MutableStateFlow(
        prefs.getBoolean("follow_system_dark_mode", true)
    )
    val followSystemDarkMode: StateFlow<Boolean> = _followSystemDarkMode.asStateFlow()

    private val _showMosqueBackgrounds = MutableStateFlow(
        prefs.getBoolean("show_mosque_backgrounds", true)
    )
    val showMosqueBackgrounds: StateFlow<Boolean> = _showMosqueBackgrounds.asStateFlow()

    fun setShowMosqueBackgrounds(enabled: Boolean) {
        _showMosqueBackgrounds.value = enabled
        prefs.edit().putBoolean("show_mosque_backgrounds", enabled).apply()
        com.example.util.MosqueBackgroundHelper.setEnabled(getApplication(), enabled)
        updateAllAppWidgets()
    }

    fun setAppThemeStyle(style: String) {
        _appThemeStyle.value = style
        prefs.edit().putString("app_theme_style", style).apply()
        val palette = when (style.lowercase()) {
            "night", "ليلي" -> "BLUE"
            "gold", "ذهبي" -> "GOLD"
            "turquoise", "تركوازي" -> "TURQUOISE"
            "purple", "أرجواني" -> "PURPLE"
            "bright_gold", "ذهبي براق" -> "BRIGHT_GOLD"
            "pale_grey", "رمادي باهت" -> "PALE_GREY"
            else -> "EMERALD"
        }
        _appThemePalette.value = palette
        updateAllAppWidgets()
    }

    fun setFollowSystemDarkMode(follow: Boolean) {
        _followSystemDarkMode.value = follow
        prefs.edit().putBoolean("follow_system_dark_mode", follow).apply()
        updateAllAppWidgets()
    }

    private val _appThemePalette = MutableStateFlow(
        when (initialStyle.lowercase()) {
            "night", "ليلي" -> "BLUE"
            "gold", "ذهبي" -> "GOLD"
            "turquoise", "تركوازي" -> "TURQUOISE"
            "purple", "أرجواني" -> "PURPLE"
            "bright_gold", "ذهبي براق" -> "BRIGHT_GOLD"
            "pale_grey", "رمادي باهت" -> "PALE_GREY"
            else -> "EMERALD"
        }
    )
    val appThemePalette: StateFlow<String> = _appThemePalette.asStateFlow()

    private val _appBackgroundTheme = MutableStateFlow(
        application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
            .getString("app_background_theme", "parchment") ?: "parchment"
    )
    val appBackgroundTheme: StateFlow<String> = _appBackgroundTheme.asStateFlow()

    fun setAppBackgroundTheme(themeId: String) {
        _appBackgroundTheme.value = themeId
        prefs.edit().putString("app_background_theme", themeId).apply()
        when (themeId) {
            "bright_gold" -> setAppThemePalette("BRIGHT_GOLD")
            "pale_grey" -> setAppThemePalette("PALE_GREY")
            "andalusian_green" -> setAppThemePalette("EMERALD")
            "desert_sand" -> setAppThemePalette("GOLD")
            "dark_night" -> setAppThemePalette("BLUE")
            else -> setAppThemePalette("GOLD")
        }
        updateAllAppWidgets()
    }

    fun cycleNextAppTheme() {
        val styles = listOf("emerald", "night", "gold", "turquoise", "purple", "bright_gold", "pale_grey")
        val current = _appThemeStyle.value.lowercase()
        val currentIndex = styles.indexOf(current)
        val nextIndex = if (currentIndex == -1 || currentIndex == styles.lastIndex) 0 else currentIndex + 1
        val nextStyle = styles[nextIndex]
        setAppThemeStyle(nextStyle)

        val matchingBgTheme = when (nextStyle) {
            "emerald" -> "emerald"
            "night" -> "dark_night"
            "gold" -> "desert_sand"
            "turquoise" -> "marble"
            "purple" -> "parchment"
            "bright_gold" -> "bright_gold"
            "pale_grey" -> "pale_grey"
            else -> "parchment"
        }
        _appBackgroundTheme.value = matchingBgTheme
        prefs.edit().putString("app_background_theme", matchingBgTheme).apply()
        updateAllAppWidgets()

        val themeLabelAr = when (nextStyle) {
            "emerald" -> "الزمردي الأندلسي 🌿"
            "night" -> "الكحلي الليلي 🌙"
            "gold" -> "الذهبي الصحراوي 🌟"
            "turquoise" -> "التركوازي الفيروزي 💧"
            "purple" -> "الأرجواني الملكي 🔮"
            "bright_gold" -> "الذهبي البراق ✨"
            "pale_grey" -> "الرمادي الهادئ 🪙"
            else -> "الزمردي 🌿"
        }
        try {
            com.example.util.ClickSoundPlayer.playClick()
            android.widget.Toast.makeText(getApplication(), "تم التبديل: $themeLabelAr", android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    fun cycleNextBackgroundTheme() {
        cycleNextAppTheme()
    }

    fun openNearestMosque(context: Context) {
        val lat = _latitude.value
        val lng = _longitude.value
        val geoUri = "geo:$lat,$lng?q=" + android.net.Uri.encode("مسجد")
        val webUri = "https://www.google.com/maps/search/" + android.net.Uri.encode("مسجد") + "/@$lat,$lng,15z"
        try {
            val mapIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUri)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {}
        }
    }

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Location & Offset State
    private val _cityName = MutableStateFlow(prefs.getString("saved_city_name", "ورقلة") ?: "ورقلة")
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private val _isRefreshingLocation = MutableStateFlow(false)
    val isRefreshingLocation: StateFlow<Boolean> = _isRefreshingLocation.asStateFlow()

    private val _countryName = MutableStateFlow(prefs.getString("saved_country_name", "الجزائر") ?: "الجزائر")
    val countryName: StateFlow<String> = _countryName.asStateFlow()

    private val _latitude = MutableStateFlow(prefs.getFloat("saved_latitude", 31.9593f).toDouble())
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(prefs.getFloat("saved_longitude", 5.3074f).toDouble())
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _prayerOffsets = MutableStateFlow(
        PrayerOffsets(
            fajr = prefs.getInt("offset_fajr", 0),
            sunrise = prefs.getInt("offset_sunrise", 0),
            dhuhr = prefs.getInt("offset_dhuhr", 0),
            asr = prefs.getInt("offset_asr", 0),
            maghrib = prefs.getInt("offset_maghrib", 0),
            isha = prefs.getInt("offset_isha", 0)
        )
    )
    val prayerOffsets: StateFlow<PrayerOffsets> = _prayerOffsets.asStateFlow()

    // Adhan Sound Selection State
    private val _selectedAdhanId = MutableStateFlow(
        (prefs.getString("selected_adhan_id", "makkah") ?: "makkah").let { if (it == "algeria" || it == "riad_algeria") "makkah" else it }
    )
    val selectedAdhanId: StateFlow<String> = _selectedAdhanId.asStateFlow()

    private val _selectedFajrAdhanId = MutableStateFlow(
        (prefs.getString("selected_fajr_adhan_id", "makkah") ?: "makkah").let { if (it == "algeria" || it == "riad_algeria") "makkah" else it }
    )
    val selectedFajrAdhanId: StateFlow<String> = _selectedFajrAdhanId.asStateFlow()

    private val _customSoundUri = MutableStateFlow<String?>(null)
    val customSoundUri: StateFlow<String?> = _customSoundUri.asStateFlow()

    private val _customSoundName = MutableStateFlow<String?>(null)
    val customSoundName: StateFlow<String?> = _customSoundName.asStateFlow()

    private val _isAdhanPlaying = MutableStateFlow(false)
    val isAdhanPlaying: StateFlow<Boolean> = _isAdhanPlaying.asStateFlow()

    private val adhanAudioPlayer = AdhanAudioPlayer(application)

    private val flipDetector = com.example.util.FlipDetector(application) {
        stopAdhanSound()
    }

    private var _lastTriggeredSunriseMin: String? = null

    // Custom Sunrise Rooster Alarm & Flip to Mute & Silent Mode Override States
    private val _useRoosterForSunrise = MutableStateFlow(prefs.getBoolean("use_rooster_for_sunrise", true))
    val useRoosterForSunrise: StateFlow<Boolean> = _useRoosterForSunrise.asStateFlow()

    fun toggleRoosterForSunrise(enabled: Boolean) {
        _useRoosterForSunrise.value = enabled
        prefs.edit().putBoolean("use_rooster_for_sunrise", enabled).apply()
        rescheduleAdhanAlarms()
    }

    private val _overrideSilentMode = MutableStateFlow(prefs.getBoolean("override_silent_mode", true))
    val overrideSilentMode: StateFlow<Boolean> = _overrideSilentMode.asStateFlow()

    fun toggleOverrideSilentMode(enabled: Boolean) {
        _overrideSilentMode.value = enabled
        prefs.edit().putBoolean("override_silent_mode", enabled).apply()
        rescheduleAdhanAlarms()
    }

    private val _useFlipToMute = MutableStateFlow(prefs.getBoolean("use_flip_to_mute", true))
    val useFlipToMute: StateFlow<Boolean> = _useFlipToMute.asStateFlow()

    fun toggleUseFlipToMute(enabled: Boolean) {
        _useFlipToMute.value = enabled
        prefs.edit().putBoolean("use_flip_to_mute", enabled).apply()
    }

    // Adhan Volume Stream: "media" (صوت الوسائط) or "alarm" (صوت التنبيه/المنبه)
    private val _adhanAudioStreamType = MutableStateFlow(prefs.getString("adhan_audio_stream_type", "media") ?: "media")
    val adhanAudioStreamType: StateFlow<String> = _adhanAudioStreamType.asStateFlow()

    fun setAdhanAudioStreamType(type: String) {
        _adhanAudioStreamType.value = type
        prefs.edit().putString("adhan_audio_stream_type", type).apply()
        rescheduleAdhanAlarms()
    }

    // App Update
    private val _availableUpdate = MutableStateFlow<com.example.util.AppUpdateResult?>(null)
    val availableUpdate: StateFlow<com.example.util.AppUpdateResult?> = _availableUpdate.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    fun checkForAppUpdates(isManual: Boolean = false, onComplete: ((com.example.util.AppUpdateResult) -> Unit)? = null) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val updateResult = com.example.util.AppUpdateManager.checkForUpdate(getApplication(), isManual = isManual)
            _isCheckingUpdate.value = false
            if (updateResult.isUpdateAvailable) {
                _availableUpdate.value = updateResult
            } else {
                _availableUpdate.value = null
            }
            onComplete?.invoke(updateResult)
        }
    }

    fun dismissUpdateNotice() {
        _availableUpdate.value = null
    }

    // Notification Style: "POPUP" (نافذة وأذان كامل) or "NOTIFICATION" (إشعار هادئ)
    private val _notificationStyle = MutableStateFlow(prefs.getString("notification_style", "POPUP") ?: "POPUP")
    val notificationStyle: StateFlow<String> = _notificationStyle.asStateFlow()

    fun setNotificationStyle(style: String) {
        _notificationStyle.value = style
        prefs.edit().putString("notification_style", style).apply()
    }

    // Lock Screen & Notification shade next prayer notification
    private val _showLockScreenPrayerNotification = MutableStateFlow(prefs.getBoolean("show_lockscreen_prayer_notification", true))
    val showLockScreenPrayerNotification: StateFlow<Boolean> = _showLockScreenPrayerNotification.asStateFlow()

    fun toggleLockScreenPrayerNotification(enabled: Boolean) {
        _showLockScreenPrayerNotification.value = enabled
        prefs.edit().putBoolean("show_lockscreen_prayer_notification", enabled).apply()
        if (enabled) {
            com.example.util.PrayerNotificationHelper.updateNotification(getApplication())
        } else {
            com.example.util.PrayerNotificationHelper.cancelNotification(getApplication())
        }
    }

    // App Language: "ar" (العربية) or "en" (English)
    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "ar") ?: "ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("app_language", lang).apply()
        try {
            com.example.widget.PrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.CompactPrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.VerticalPrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.MorningAdhkarWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.EveningAdhkarWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.NextPrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.util.PrayerNotificationHelper.updateNotification(getApplication())
        } catch (_: Exception) {}
    }

    // Calculation Method Setting (Default is 16: Algerian Ministry)
    private val _calculationMethodId = MutableStateFlow(prefs.getInt("calculation_method_id", 16))
    val calculationMethodId: StateFlow<Int> = _calculationMethodId.asStateFlow()

    fun setCalculationMethod(methodId: Int) {
        _calculationMethodId.value = methodId
        prefs.edit().putInt("calculation_method_id", methodId).apply()
        loadPrayerTimings()
    }

    // Per-Prayer Adhan Control Settings (Fajr, Dhuhr, Asr, Maghrib, Isha, 24h format)
    private val _prayerAdhanSettings = MutableStateFlow(
        PrayerAdhanSettings(
            fajr = prefs.getBoolean("adhan_fajr", true),
            dhuhr = prefs.getBoolean("adhan_dhuhr", true),
            asr = prefs.getBoolean("adhan_asr", true),
            maghrib = prefs.getBoolean("adhan_maghrib", true),
            isha = prefs.getBoolean("adhan_isha", true),
            is24HourFormat = prefs.getBoolean("is_24_hour_format", true)
        )
    )
    val prayerAdhanSettings: StateFlow<PrayerAdhanSettings> = _prayerAdhanSettings.asStateFlow()

    fun togglePrayerAdhan(prayerKey: String, enabled: Boolean) {
        val current = _prayerAdhanSettings.value
        val updated = when (prayerKey.lowercase()) {
            "fajr" -> current.copy(fajr = enabled)
            "dhuhr" -> current.copy(dhuhr = enabled)
            "asr" -> current.copy(asr = enabled)
            "maghrib" -> current.copy(maghrib = enabled)
            "isha" -> current.copy(isha = enabled)
            else -> current
        }
        _prayerAdhanSettings.value = updated
        prefs.edit().putBoolean("adhan_${prayerKey.lowercase()}", enabled).apply()
        AdhanScheduler.getPrefs(getApplication()).edit()
            .putBoolean("adhan_${prayerKey.lowercase()}", enabled)
            .apply()
        rescheduleAdhanAlarms()
    }

    fun toggle24HourFormat(enabled: Boolean) {
        val current = _prayerAdhanSettings.value
        val updated = current.copy(is24HourFormat = enabled)
        _prayerAdhanSettings.value = updated
        prefs.edit().putBoolean("is_24_hour_format", enabled).apply()
        updateAllAppWidgets()
    }

    // Prayer State
    private val _prayerTimings = MutableStateFlow<PrayerTimings?>(null)
    val prayerTimings: StateFlow<PrayerTimings?> = _prayerTimings.asStateFlow()

    private val _hijriDate = MutableStateFlow<String?>("")
    val hijriDate: StateFlow<String?> = _hijriDate.asStateFlow()

    private val _hijriOffset = MutableStateFlow(0)
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

    private val _asrMethod = MutableStateFlow(
        prefs.getString("asr_method", "standard") ?: "standard"
    )
    val asrMethod: StateFlow<String> = _asrMethod.asStateFlow()

    fun setAsrMethod(method: String) {
        _asrMethod.value = method
        prefs.edit().putString("asr_method", method).apply()
        AdhanScheduler.getPrefs(getApplication()).edit()
            .putString("asr_method", method)
            .apply()
        loadPrayerTimings()
    }

    private val _fajrFormulaWithTatweeb = MutableStateFlow(
        prefs.getBoolean("fajr_formula_tatweeb", true)
    )
    val fajrFormulaWithTatweeb: StateFlow<Boolean> = _fajrFormulaWithTatweeb.asStateFlow()

    fun toggleFajrFormulaWithTatweeb(enabled: Boolean) {
        _fajrFormulaWithTatweeb.value = enabled
        prefs.edit().putBoolean("fajr_formula_tatweeb", enabled).apply()
        AdhanScheduler.getPrefs(getApplication()).edit()
            .putBoolean("fajr_formula_tatweeb", enabled)
            .apply()
        rescheduleAdhanAlarms()
    }

    private val _rawHijriDate = MutableStateFlow<String?>("")

    private val _nextPrayer = MutableStateFlow<NextPrayerInfo?>(null)
    val nextPrayer: StateFlow<NextPrayerInfo?> = _nextPrayer.asStateFlow()

    private val _countdownText = MutableStateFlow("00:00:00")
    val countdownText: StateFlow<String> = _countdownText.asStateFlow()

    private val _currentTimeText = MutableStateFlow("--:--:--")
    val currentTimeText: StateFlow<String> = _currentTimeText.asStateFlow()

    private val _isPrayerLoading = MutableStateFlow(false)
    val isPrayerLoading: StateFlow<Boolean> = _isPrayerLoading.asStateFlow()

    // Quran Customization State
    private val _quranEdition = MutableStateFlow("quran-uthmani") // "quran-uthmani" or "quran-warsh"
    val quranEdition: StateFlow<String> = _quranEdition.asStateFlow()

    private val _quranFontSize = MutableStateFlow(
        prefs.getFloat("quran_font_size", 24f)
    )
    val quranFontSize: StateFlow<Float> = _quranFontSize.asStateFlow()

    private val _quranFontType = MutableStateFlow("uthmani")
    val quranFontType: StateFlow<String> = _quranFontType.asStateFlow()

    private val _allSurahs = MutableStateFlow<List<SurahItem>>(emptyList())
    val allSurahs: StateFlow<List<SurahItem>> = _allSurahs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSurah = MutableStateFlow<SurahDetail?>(null)
    val selectedSurah: StateFlow<SurahDetail?> = _selectedSurah.asStateFlow()

    private val _targetScrollAyah = MutableStateFlow<Int?>(null)
    val targetScrollAyah: StateFlow<Int?> = _targetScrollAyah.asStateFlow()

    private val _isQuranFullScreen = MutableStateFlow(false)
    val isQuranFullScreen: StateFlow<Boolean> = _isQuranFullScreen.asStateFlow()

    private val _isQuranLoading = MutableStateFlow(false)
    val isQuranLoading: StateFlow<Boolean> = _isQuranLoading.asStateFlow()

    private val _quranError = MutableStateFlow<String?>(null)
    val quranError: StateFlow<String?> = _quranError.asStateFlow()

    private val _downloadedSurahs = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedSurahs: StateFlow<Set<Int>> = _downloadedSurahs.asStateFlow()

    fun refreshDownloadedSurahs(surahsList: List<SurahItem> = _allSurahs.value) {
        if (surahsList.isEmpty()) return
        val reciterId = _selectedReciterId.value
        viewModelScope.launch(Dispatchers.IO) {
            val set = com.example.util.QuranStorageManager.getDownloadedSurahNumbers(
                context = getApplication(),
                reciterIdentifier = reciterId,
                allSurahs = surahsList
            )
            _downloadedSurahs.value = set
        }
    }

    // Quran Audio & Memorization Repeat Engine
    private val quranAudioPlayer = com.example.util.QuranAudioPlayer(application)

    private val _selectedReciterId = MutableStateFlow("ar.alijaber")
    val selectedReciterId: StateFlow<String> = _selectedReciterId.asStateFlow()

    private val _startAyah = MutableStateFlow(1)
    val startAyah: StateFlow<Int> = _startAyah.asStateFlow()

    private val _endAyah = MutableStateFlow(1)
    val endAyah: StateFlow<Int> = _endAyah.asStateFlow()

    private val _repeatPerAyah = MutableStateFlow(1) // 1, 3, 5, 999
    val repeatPerAyah: StateFlow<Int> = _repeatPerAyah.asStateFlow()

    private val _playingAyahNumber = MutableStateFlow<Int?>(null)
    val playingAyahNumber: StateFlow<Int?> = _playingAyahNumber.asStateFlow()

    private val _isPlayingQuranAudio = MutableStateFlow(false)
    val isPlayingQuranAudio: StateFlow<Boolean> = _isPlayingQuranAudio.asStateFlow()

    private val _isQuranAudioPaused = MutableStateFlow(false)
    val isQuranAudioPaused: StateFlow<Boolean> = _isQuranAudioPaused.asStateFlow()

    private val _playbackProgressRatio = MutableStateFlow(0f)
    val playbackProgressRatio: StateFlow<Float> = _playbackProgressRatio.asStateFlow()

    private val _recitationNetworkError = MutableStateFlow<String?>(null)
    val recitationNetworkError: StateFlow<String?> = _recitationNetworkError.asStateFlow()

    fun dismissRecitationError() {
        _recitationNetworkError.value = null
    }

    private var currentRepeatCounter = 0

    // Bookmark
    private var bookmarkJob: Job? = null
    private val _bookmark = MutableStateFlow<BookmarkEntity?>(null)
    val bookmark: StateFlow<BookmarkEntity?> = _bookmark.asStateFlow()

    private fun observeBookmarkForEdition(edition: String) {
        bookmarkJob?.cancel()
        bookmarkJob = viewModelScope.launch {
            quranRepo.getBookmark(edition).collect {
                _bookmark.value = it
            }
        }
    }

    private var clockJob: Job? = null

    private val timeChangeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            java.util.TimeZone.setDefault(null)
            loadPrayerTimings()
        }
    }

    init {
        quranAudioPlayer.onProgressUpdate = { pos, dur ->
            _playbackProgressRatio.value = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
        }

        quranAudioPlayer.onError = { message ->
            viewModelScope.launch(Dispatchers.Main) {
                stopQuranAudio()
                _recitationNetworkError.value = message
                android.widget.Toast.makeText(application, message, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_TIMEZONE_CHANGED)
            addAction(android.content.Intent.ACTION_TIME_CHANGED)
            addAction(android.content.Intent.ACTION_DATE_CHANGED)
        }
        try {
            application.registerReceiver(timeChangeReceiver, filter)
        } catch (_: Exception) {}

        rescheduleAdhanAlarms()
        loadPrayerTimings()
        loadSurahs()
        startClockLoop()
        observeBookmarkForEdition(_quranEdition.value)
        checkAndAutoUpdateLocation()
        startContinuousLocationUpdates()

        viewModelScope.launch {
            kotlinx.coroutines.delay(3000L)
            com.example.util.AppUpdateManager.performCleanupIfUpToDate(getApplication())
            checkForAppUpdates(isManual = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(timeChangeReceiver)
        } catch (_: Exception) {}
        stopContinuousLocationUpdates()
        flipDetector.stopListening()
        adhanAudioPlayer.stopSound()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleDarkMode() {
        _followSystemDarkMode.value = false
        prefs.edit().putBoolean("follow_system_dark_mode", false).apply()
        _isDarkMode.value = !_isDarkMode.value
        prefs.edit().putBoolean("is_dark_mode", _isDarkMode.value).apply()
        updateAllAppWidgets()
    }

    fun setDarkMode(isDark: Boolean) {
        _followSystemDarkMode.value = false
        prefs.edit().putBoolean("follow_system_dark_mode", false).apply()
        _isDarkMode.value = isDark
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        updateAllAppWidgets()
    }

    private fun updateAllAppWidgets() {
        try {
            com.example.util.NextPrayerTracker.syncAll(getApplication())
            com.example.widget.MorningAdhkarWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.EveningAdhkarWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.VerticalPrayerWidgetProvider.updateAllWidgets(getApplication())
        } catch (_: Exception) {}
    }

    fun setAppThemePalette(palette: String) {
        _appThemePalette.value = palette
    }

    fun cycleNextThemePalette() {
        val palettes = listOf("EMERALD", "BLUE", "GOLD", "PURPLE", "TURQUOISE")
        val currentIndex = palettes.indexOf(_appThemePalette.value)
        val nextIndex = (if (currentIndex != -1) currentIndex + 1 else 0) % palettes.size
        _appThemePalette.value = palettes[nextIndex]
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        rescheduleAdhanAlarms()
    }

    fun updateLocation(city: String, country: String, lat: Double, lng: Double) {
        _cityName.value = city
        _countryName.value = country
        _latitude.value = lat
        _longitude.value = lng
        prefs.edit()
            .putString("saved_city_name", city)
            .putString("saved_country_name", country)
            .putFloat("saved_latitude", lat.toFloat())
            .putFloat("saved_longitude", lng.toFloat())
            .commit()

        AdhanScheduler.getPrefs(getApplication()).edit()
            .putString("saved_city_name", city)
            .putString("saved_country_name", country)
            .putFloat("saved_latitude", lat.toFloat())
            .putFloat("saved_longitude", lng.toFloat())
            .commit()

        loadPrayerTimings()
        try {
            com.example.util.NextPrayerTracker.syncAll(getApplication())
        } catch (_: Exception) {}
    }

    fun refreshLocationAndPrayers() {
        if (_isRefreshingLocation.value) return
        _isRefreshingLocation.value = true
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                // Safety timeout in case GPS takes longer than 6 seconds
                val timeoutJob = viewModelScope.launch {
                    kotlinx.coroutines.delay(6000L)
                    if (_isRefreshingLocation.value) {
                        _isRefreshingLocation.value = false
                    }
                }

                com.example.ui.components.getGpsLocation(context) { loc ->
                    timeoutJob.cancel()
                    if (loc != null) {
                        com.example.ui.components.resolveLocationAddress(context, loc) { city, country ->
                            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                updateLocation(city, country, loc.latitude, loc.longitude)
                                _isRefreshingLocation.value = false
                            }
                        }
                    } else {
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            loadPrayerTimings()
                            try {
                                com.example.util.NextPrayerTracker.syncAll(context)
                            } catch (_: Exception) {}
                            _isRefreshingLocation.value = false
                        }
                    }
                }
            } catch (_: Exception) {
                _isRefreshingLocation.value = false
            }
        }
    }

    fun checkAndAutoUpdateLocation() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                val isGpsEnabled = lm?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
                        lm?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
                if (isGpsEnabled) {
                    com.example.ui.components.getGpsLocation(context) { loc ->
                        if (loc != null) {
                            val savedLat = _latitude.value
                            val savedLng = _longitude.value
                            val results = FloatArray(1)
                            android.location.Location.distanceBetween(
                                savedLat, savedLng,
                                loc.latitude, loc.longitude,
                                results
                            )
                            val distanceInMeters = results[0]
                            if (distanceInMeters > 300.0f) {
                                com.example.ui.components.resolveLocationAddress(context, loc) { city, country ->
                                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                        updateLocation(city, country, loc.latitude, loc.longitude)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private var continuousLocationListener: android.location.LocationListener? = null

    fun startContinuousLocationUpdates() {
        try {
            val context = getApplication<Application>()
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return
            if (continuousLocationListener != null) return

            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(loc: android.location.Location) {
                    val savedLat = _latitude.value
                    val savedLng = _longitude.value
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        savedLat, savedLng,
                        loc.latitude, loc.longitude,
                        results
                    )
                    val distanceInMeters = results[0]
                    if (distanceInMeters > 300.0f) {
                        com.example.ui.components.resolveLocationAddress(context, loc) { city, country ->
                            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                updateLocation(city, country, loc.latitude, loc.longitude)
                            }
                        }
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            continuousLocationListener = listener

            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER
            )
            for (provider in providers) {
                if (lm.isProviderEnabled(provider)) {
                    try {
                        lm.requestLocationUpdates(provider, 15000L, 100f, listener, android.os.Looper.getMainLooper())
                    } catch (_: SecurityException) {} catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    fun stopContinuousLocationUpdates() {
        try {
            val context = getApplication<Application>()
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            continuousLocationListener?.let { lm?.removeUpdates(it) }
            continuousLocationListener = null
        } catch (_: Exception) {}
    }

    fun updateOffsets(newOffsets: PrayerOffsets) {
        _prayerOffsets.value = newOffsets
        prefs.edit()
            .putInt("offset_fajr", newOffsets.fajr)
            .putInt("offset_sunrise", newOffsets.sunrise)
            .putInt("offset_dhuhr", newOffsets.dhuhr)
            .putInt("offset_asr", newOffsets.asr)
            .putInt("offset_maghrib", newOffsets.maghrib)
            .putInt("offset_isha", newOffsets.isha)
            .apply()
        loadPrayerTimings()
    }

    fun updateHijriOffset(offset: Int) {
        _hijriOffset.value = offset
        recalculateHijriDate()
    }

    fun updateQuranFontSize(size: Float) {
        val bounded = size.coerceIn(18f, 44f)
        _quranFontSize.value = bounded
        prefs.edit().putFloat("quran_font_size", bounded).apply()
    }

    fun updateQuranFontType(type: String) {
        _quranFontType.value = type
        if (type == "warsh") {
            setQuranEdition("quran-warsh")
        } else {
            setQuranEdition("quran-uthmani")
        }
    }

    private fun recalculateHijriDate() {
        val raw = _rawHijriDate.value ?: return
        _hijriDate.value = adjustHijriString(raw, _hijriOffset.value)
    }

    private fun adjustHijriString(raw: String, offsetDays: Int): String {
        if (raw.isEmpty() || offsetDays == 0) return raw
        val parts = raw.trim().split(" ")
        if (parts.isNotEmpty()) {
            val dayInt = parts[0].toIntOrNull()
            if (dayInt != null) {
                val newDay = (dayInt + offsetDays).coerceIn(1, 30)
                val rest = parts.subList(1, parts.size).joinToString(" ")
                return "$newDay $rest"
            }
        }
        return raw
    }

    fun updateAdhanSound(soundId: String, fajrSoundId: String, customUri: String?, customName: String?) {
        _selectedAdhanId.value = soundId
        _selectedFajrAdhanId.value = fajrSoundId
        _customSoundUri.value = customUri
        _customSoundName.value = customName
        prefs.edit()
            .putString("selected_adhan_id", soundId)
            .putString("selected_sound_id", soundId)
            .putString("selected_fajr_adhan_id", fajrSoundId)
            .putString("selected_fajr_sound_id", fajrSoundId)
            .putString("custom_sound_uri", customUri)
            .putString("custom_sound_name", customName)
            .apply()
        AdhanScheduler.getPrefs(getApplication()).edit()
            .putString("selected_sound_id", soundId)
            .putString("selected_adhan_id", soundId)
            .putString("selected_fajr_sound_id", fajrSoundId)
            .putString("selected_fajr_adhan_id", fajrSoundId)
            .putString("custom_sound_uri", customUri ?: "")
            .apply()
        rescheduleAdhanAlarms()
    }

    fun rescheduleAdhanAlarms() {
        val timings = _prayerTimings.value ?: AdhanScheduler.getSavedPrayerTimings(getApplication()) ?: return
        if (_isMuted.value) {
            AdhanScheduler.cancelAllPrayers(getApplication())
            return
        }
        AdhanScheduler.saveAdhanConfig(
            context = getApplication(),
            selectedSoundId = _selectedAdhanId.value,
            selectedFajrSoundId = _selectedFajrAdhanId.value,
            customSoundUri = _customSoundUri.value,
            overrideSilentMode = _overrideSilentMode.value,
            useRoosterForSunrise = _useRoosterForSunrise.value,
            prayerSettings = _prayerAdhanSettings.value,
            audioStreamType = _adhanAudioStreamType.value
        )
        AdhanScheduler.scheduleAllPrayers(getApplication(), timings)
        try {
            com.example.widget.PrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.CompactPrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.VerticalPrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.MorningAdhkarWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.EveningAdhkarWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.NextPrayerWidgetProvider.updateAllWidgets(getApplication())
            com.example.util.PrayerNotificationHelper.updateNotification(getApplication())
        } catch (_: Exception) {}
    }

    fun triggerAdhan(prayerKey: String? = null) {
        if (_isMuted.value) return

        if (prayerKey?.lowercase() == "sunrise") {
            if (!_useRoosterForSunrise.value) return
            _isAdhanPlaying.value = true
            if (_useFlipToMute.value) {
                flipDetector.startListening()
            }
            adhanAudioPlayer.playSound("raw_rooster", streamType = _adhanAudioStreamType.value) {
                _isAdhanPlaying.value = false
                flipDetector.stopListening()
            }
            return
        }

        if (prayerKey != null && !_prayerAdhanSettings.value.isEnabledFor(prayerKey)) {
            // Adhan is disabled by user for this specific prayer
            return
        }

        val isFajr = prayerKey?.lowercase() == "fajr"

        val soundUrl = if (_selectedAdhanId.value == "custom_local" && !_customSoundUri.value.isNullOrEmpty()) {
            _customSoundUri.value
        } else if (isFajr) {
            val selected = PRESET_ADHAN_SOUNDS.find { it.id == _selectedFajrAdhanId.value } ?: PRESET_ADHAN_SOUNDS.first()
            selected.fajrAudioUrl
        } else {
            val selected = PRESET_ADHAN_SOUNDS.find { it.id == _selectedAdhanId.value } ?: PRESET_ADHAN_SOUNDS.first()
            selected.audioUrl
        }

        if (soundUrl != null) {
            _isAdhanPlaying.value = true
            if (_useFlipToMute.value) {
                flipDetector.startListening()
            }
            adhanAudioPlayer.playSound(soundUrl, overrideSilentMode = _overrideSilentMode.value, streamType = _adhanAudioStreamType.value) {
                _isAdhanPlaying.value = false
                flipDetector.stopListening()
                try {
                    val pKey = prayerKey?.lowercase() ?: "dhuhr"
                    if (pKey != "sunrise") {
                        com.example.util.PrayerAutoSilentManager.onPrayerTriggered(
                            getApplication(),
                            pKey,
                            _nextPrayer.value?.nameAr ?: "الصلاة",
                            isAdhanAlreadyPlayed = true
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun stopAdhanSound() {
        flipDetector.stopListening()
        adhanAudioPlayer.stopSound()
        _isAdhanPlaying.value = false
        try {
            val pKey = _nextPrayer.value?.nameEn?.lowercase() ?: "dhuhr"
            if (pKey != "sunrise") {
                com.example.util.PrayerAutoSilentManager.onPrayerTriggered(
                    getApplication(),
                    pKey,
                    _nextPrayer.value?.nameAr ?: "الصلاة",
                    isAdhanAlreadyPlayed = true
                )
            }
        } catch (_: Exception) {}
    }

    fun loadPrayerTimings() {
        viewModelScope.launch {
            _isPrayerLoading.value = true
            val result = prayerRepo.fetchPrayerTimings(
                city = _cityName.value,
                latitude = _latitude.value,
                longitude = _longitude.value,
                offsets = _prayerOffsets.value,
                method = _calculationMethodId.value,
                asrMethod = _asrMethod.value
            )
            result.onSuccess { (timings, hijri) ->
                _prayerTimings.value = timings
                _rawHijriDate.value = hijri
                val finalHijri = adjustHijriString(hijri ?: "", _hijriOffset.value)
                _hijriDate.value = finalHijri
                prefs.edit()
                    .putString("saved_city_name", _cityName.value)
                    .putString("saved_country_name", _countryName.value)
                    .putFloat("saved_latitude", _latitude.value.toFloat())
                    .putFloat("saved_longitude", _longitude.value.toFloat())
                    .putString("saved_hijri_date", finalHijri)
                    .apply()
                recalculateNextPrayer()
                rescheduleAdhanAlarms()
            }
            _isPrayerLoading.value = false
        }
    }

    fun pauseClockLoop() {
        clockJob?.cancel()
        clockJob = null
    }

    fun resumeClockLoop() {
        syncClockImmediately()
        startClockLoop()
    }

    private fun syncClockImmediately() {
        val now = System.currentTimeMillis()
        _currentTimeText.value = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(now))
        val next = com.example.util.NextPrayerTracker.getNextPrayer(getApplication())
        val remMillis = com.example.util.NextPrayerTracker.getRemainingMillis(next.targetTimeMillis, now)
        _nextPrayer.value = NextPrayerInfo(
            nameAr = next.nameAr,
            nameEn = next.nameEn,
            formattedTime = next.formattedTime,
            remainingMillis = remMillis
        )
        _countdownText.value = com.example.util.NextPrayerTracker.formatRemainingTime(remMillis)
    }

    private fun recalculateNextPrayer() {
        syncClockImmediately()
    }

    private fun startClockLoop() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentTimeText.value = timeFormat.format(Date(now))

                val timings = _prayerTimings.value
                val next = com.example.util.NextPrayerTracker.getNextPrayer(getApplication())
                val remMillis = com.example.util.NextPrayerTracker.getRemainingMillis(next.targetTimeMillis, now)

                _nextPrayer.value = NextPrayerInfo(
                    nameAr = next.nameAr,
                    nameEn = next.nameEn,
                    formattedTime = next.formattedTime,
                    remainingMillis = remMillis
                )
                // Requirement 7: Unified rounding matching native Chronometer
                _countdownText.value = com.example.util.NextPrayerTracker.formatRemainingTime(remMillis)

                if (timings != null) {
                    val currentHM = SimpleDateFormat("HH:mm", Locale.US).format(Date(now))
                    val sunriseHM = timings.sunrise.take(5)
                    if (currentHM == sunriseHM && _lastTriggeredSunriseMin != currentHM) {
                        _lastTriggeredSunriseMin = currentHM
                        triggerAdhan("sunrise")
                    }
                }

                if (remMillis <= 0L) {
                    triggerAdhan(next.nameEn)
                    com.example.util.NextPrayerTracker.syncAll(getApplication())
                    delay(2000)
                    loadPrayerTimings()
                    continue
                }

                // Requirement 2: Align tick delay to the second boundary: 1000 - (now % 1000)
                val currentMs = System.currentTimeMillis()
                val delayMs = maxOf(50L, 1000L - (currentMs % 1000L))
                delay(delayMs)
            }
        }
    }

    fun loadSurahs() {
        viewModelScope.launch {
            _isQuranLoading.value = true
            _quranError.value = null
            val result = quranRepo.getSurahsList()
            result.onSuccess { list ->
                _allSurahs.value = list
                refreshDownloadedSurahs(list)
            }.onFailure { err ->
                _quranError.value = err.message ?: "فشل تحميل قائمة السور"
            }
            _isQuranLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setQuranEdition(edition: String) {
        if (_quranEdition.value != edition) {
            _quranEdition.value = edition
            observeBookmarkForEdition(edition)
            val currentSurah = _selectedSurah.value
            if (currentSurah != null) {
                openSurah(currentSurah.number)
            }
        }
    }

    fun openSurah(surahNumber: Int, targetAyah: Int? = null) {
        viewModelScope.launch {
            _isQuranLoading.value = true
            _quranError.value = null
            _targetScrollAyah.value = targetAyah
            stopQuranAudio()
            val result = quranRepo.getSurahDetail(surahNumber, _quranEdition.value)
            result.onSuccess { detail ->
                _selectedSurah.value = detail
                _startAyah.value = 1
                _endAyah.value = detail.ayahs.size
            }.onFailure { err ->
                _quranError.value = err.message ?: "فشل تحميل آيات السورة"
            }
            _isQuranLoading.value = false
        }
    }

    fun clearTargetScrollAyah() {
        _targetScrollAyah.value = null
    }

    fun closeSurahView() {
        stopQuranAudio()
        _isQuranFullScreen.value = false
        _selectedSurah.value = null
        _targetScrollAyah.value = null
    }

    fun toggleQuranFullScreen() {
        _isQuranFullScreen.value = !_isQuranFullScreen.value
    }

    fun setQuranFullScreen(fullScreen: Boolean) {
        _isQuranFullScreen.value = fullScreen
    }

    fun setReciterId(reciterId: String) {
        _selectedReciterId.value = reciterId
        if (_isPlayingQuranAudio.value) {
            stopQuranAudio()
        }
        refreshDownloadedSurahs()
    }

    fun setAyahRange(start: Int, end: Int) {
        val surah = _selectedSurah.value ?: return
        val maxAyahs = surah.ayahs.size
        _startAyah.value = start.coerceIn(1, maxAyahs)
        _endAyah.value = end.coerceIn(_startAyah.value, maxAyahs)
    }

    fun setRepeatPerAyah(count: Int) {
        _repeatPerAyah.value = count
    }

    fun togglePlayQuranAudio(targetAyah: Int? = null) {
        val surah = _selectedSurah.value ?: return
        if (_isPlayingQuranAudio.value && targetAyah == null) {
            if (_isQuranAudioPaused.value) {
                resumeQuranAudio()
            } else {
                pauseQuranAudio()
            }
        } else {
            val start = targetAyah ?: _startAyah.value.coerceIn(1, surah.ayahs.size)

            // Check if audio is downloaded locally or internet connection is active
            val fileInternal = com.example.util.QuranStorageManager.getLocalAudioFile(getApplication(), false, _selectedReciterId.value, surah.number, start)
            val fileSd = com.example.util.QuranStorageManager.getLocalAudioFile(getApplication(), true, _selectedReciterId.value, surah.number, start)
            val isDownloaded = (fileInternal != null && fileInternal.exists() && fileInternal.length() > 1000) ||
                    (fileSd != null && fileSd.exists() && fileSd.length() > 1000)

            if (!isDownloaded && !com.example.util.NetworkUtils.isNetworkAvailable(getApplication())) {
                val msg = "لا يوجد اتصال بالإنترنت لتشغيل التلاوة. يرجى الاتصال بالإنترنت أو تنزيل السورة للاستماع بدون إنترنت."
                _recitationNetworkError.value = msg
                android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_LONG).show()
                return
            }

            _recitationNetworkError.value = null
            _startAyah.value = start
            if (targetAyah != null) {
                _endAyah.value = targetAyah
            } else if (_endAyah.value < start) {
                _endAyah.value = surah.ayahs.size
            }
            _isPlayingQuranAudio.value = true
            _isQuranAudioPaused.value = false
            currentRepeatCounter = 0
            playCurrentAyahStep(surah.number, start)
        }
    }

    fun pauseQuranAudio() {
        if (_isPlayingQuranAudio.value && !_isQuranAudioPaused.value) {
            quranAudioPlayer.pause()
            _isQuranAudioPaused.value = true
        }
    }

    fun resumeQuranAudio() {
        if (_isPlayingQuranAudio.value && _isQuranAudioPaused.value) {
            quranAudioPlayer.resume()
            _isQuranAudioPaused.value = false
        }
    }

    private fun playCurrentAyahStep(surahNumber: Int, ayahNum: Int) {
        val fileInternal = com.example.util.QuranStorageManager.getLocalAudioFile(getApplication(), false, _selectedReciterId.value, surahNumber, ayahNum)
        val fileSd = com.example.util.QuranStorageManager.getLocalAudioFile(getApplication(), true, _selectedReciterId.value, surahNumber, ayahNum)
        val isDownloaded = (fileInternal != null && fileInternal.exists() && fileInternal.length() > 1000) ||
                (fileSd != null && fileSd.exists() && fileSd.length() > 1000)

        if (!isDownloaded && !com.example.util.NetworkUtils.isNetworkAvailable(getApplication())) {
            val msg = "انقطع الاتصال بالإنترنت أثناء تشغيل التلاوة."
            _recitationNetworkError.value = msg
            android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_LONG).show()
            stopQuranAudio()
            return
        }

        _playingAyahNumber.value = ayahNum
        _isPlayingQuranAudio.value = true
        _isQuranAudioPaused.value = false

        quranAudioPlayer.playAyah(
            reciterIdentifier = _selectedReciterId.value,
            surahNumber = surahNumber,
            ayahNumberInSurah = ayahNum,
            onCompletion = {
                viewModelScope.launch {
                    onAyahAudioCompleted(surahNumber, ayahNum)
                }
            }
        )
    }

    private fun onAyahAudioCompleted(surahNumber: Int, ayahNum: Int) {
        if (!_isPlayingQuranAudio.value) return

        val maxRepeats = _repeatPerAyah.value.coerceAtLeast(1)
        if (currentRepeatCounter + 1 < maxRepeats) {
            currentRepeatCounter++
            playCurrentAyahStep(surahNumber, ayahNum)
        } else {
            currentRepeatCounter = 0
            if (ayahNum < _endAyah.value) {
                val nextAyah = ayahNum + 1
                playCurrentAyahStep(surahNumber, nextAyah)
            } else {
                stopQuranAudio()
            }
        }
    }

    fun stopQuranAudio() {
        quranAudioPlayer.stop()
        _isPlayingQuranAudio.value = false
        _isQuranAudioPaused.value = false
        _playingAyahNumber.value = null
        _playbackProgressRatio.value = 0f
        currentRepeatCounter = 0
    }

    fun saveBookmark(surahNumber: Int, surahName: String, ayahNumber: Int) {
        viewModelScope.launch {
            quranRepo.saveBookmark(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                edition = _quranEdition.value
            )
        }
    }

    // Numeral system setting: "western" (العربية الغبارية: 1, 2, 3) or "eastern" (المشرقية/الهندية: ١، ٢، ٣)
    private val _numeralSystem = MutableStateFlow(
        prefs.getString("numeral_system", "western") ?: "western"
    )
    val numeralSystem: StateFlow<String> = _numeralSystem.asStateFlow()

    fun setNumeralSystem(system: String) {
        _numeralSystem.value = system
        prefs.edit().putString("numeral_system", system).apply()
        updateAllAppWidgets()
        val msg = if (system == "eastern") "تم تفعيل الأرقام المشرقية (الهندية: ١، ٢، ٣)" else "تم تفعيل الأرقام العربية (الغبارية: 1, 2, 3)"
        android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    // Storage and Download Customization Engine
    private val _useSdCardForDownloads = MutableStateFlow(
        prefs.getBoolean("use_sd_card_for_downloads", false)
    )
    val useSdCardForDownloads: StateFlow<Boolean> = _useSdCardForDownloads.asStateFlow()

    fun setUseSdCardForDownloads(enabled: Boolean) {
        if (enabled && !com.example.util.QuranStorageManager.hasSdCard(getApplication())) {
            android.widget.Toast.makeText(getApplication(), "تعذر التبديل: لا توجد بطاقة ذاكرة SD موصولة بالجهاز", android.widget.Toast.LENGTH_LONG).show()
            _useSdCardForDownloads.value = false
            prefs.edit().putBoolean("use_sd_card_for_downloads", false).apply()
            return
        }
        _useSdCardForDownloads.value = enabled
        prefs.edit().putBoolean("use_sd_card_for_downloads", enabled).apply()
        refreshDownloadedSurahs()
        val msg = if (enabled) "تم تحويل مكان الحفظ إلى بطاقة الذاكرة الخارجية (SD Card)" else "تم تحويل مكان الحفظ إلى ذاكرة الهاتف الداخلية"
        android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    private val _downloadingSurahNumber = MutableStateFlow<Int?>(null)
    val downloadingSurahNumber: StateFlow<Int?> = _downloadingSurahNumber.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadStatus = MutableStateFlow<String?>(null)
    val downloadStatus: StateFlow<String?> = _downloadStatus.asStateFlow()

    private val _isDownloadingAllTexts = MutableStateFlow(false)
    val isDownloadingAllTexts: StateFlow<Boolean> = _isDownloadingAllTexts.asStateFlow()

    fun downloadSurah(surahNumber: Int, reciterId: String, totalAyahs: Int) {
        if (_downloadingSurahNumber.value != null || _isDownloadingAllTexts.value) {
            _downloadStatus.value = "هناك عملية تحميل أخرى قيد التشغيل حالياً."
            return
        }
        val freeBytes = com.example.util.QuranStorageManager.getStorageFreeSpace(getApplication(), _useSdCardForDownloads.value)
        if (freeBytes > 0 && freeBytes < 30 * 1024 * 1024) {
            _downloadStatus.value = "المساحة التخزينية المتبقية غير كافية لإتمام التحميل (أقل من 30 ميجابايت)."
            return
        }
        viewModelScope.launch {
            _downloadingSurahNumber.value = surahNumber
            _downloadProgress.value = 0f
            _downloadStatus.value = "جاري بدء تحميل سورة رقم $surahNumber..."

            val folder = com.example.util.RECITER_FOLDER_MAP[reciterId] ?: "Alafasy_128kbps"
            var successCount = 0

            for (i in 1..totalAyahs) {
                _downloadStatus.value = "جاري تحميل الآية $i من $totalAyahs..."
                val success = com.example.util.QuranStorageManager.downloadAyah(
                    getApplication(),
                    _useSdCardForDownloads.value,
                    folder,
                    reciterId,
                    surahNumber,
                    i
                )
                if (success) {
                    successCount++
                }
                _downloadProgress.value = i.toFloat() / totalAyahs
            }

            if (successCount == totalAyahs) {
                _downloadStatus.value = "تم تحميل السورة بنجاح وبشكل كامل!"
                refreshDownloadedSurahs()
            } else {
                _downloadStatus.value = "اكتمل التحميل مع فشل بعض الآيات ($successCount من $totalAyahs)."
                refreshDownloadedSurahs()
            }
            kotlinx.coroutines.delay(3000)
            _downloadingSurahNumber.value = null
            _downloadProgress.value = 0f
            _downloadStatus.value = null
        }
    }

    fun downloadAllQuranTexts() {
        if (_isDownloadingAllTexts.value || _downloadingSurahNumber.value != null) return
        android.widget.Toast.makeText(getApplication(), "بدأ تحميل نصوص المصحف بالكامل... يرجى الانتظار", android.widget.Toast.LENGTH_LONG).show()
        viewModelScope.launch {
            _isDownloadingAllTexts.value = true
            _downloadStatus.value = "جاري تحميل نصوص المصحف الشريف للعمل دون إنترنت..."
            var count = 0
            val list = _allSurahs.value.ifEmpty {
                val listResult = quranRepo.getSurahsList()
                listResult.getOrDefault(emptyList())
            }
            if (list.isEmpty()) {
                _downloadStatus.value = "فشل بدء التحميل: تعذر جلب قائمة السور."
                _isDownloadingAllTexts.value = false
                return@launch
            }

            for (surah in list) {
                _downloadStatus.value = "تحميل نصوص سورة ${surah.name}..."
                // Pre-fetch both Hafs and Warsh editions so they are both 100% offline
                quranRepo.getSurahDetail(surah.number, "quran-uthmani")
                quranRepo.getSurahDetail(surah.number, "quran-warsh")
                count++
                _downloadProgress.value = count.toFloat() / list.size
            }

            _downloadStatus.value = getApplication<Application>().getString(R.string.download_status_success)
            kotlinx.coroutines.delay(3000)
            _isDownloadingAllTexts.value = false
            _downloadProgress.value = 0f
            _downloadStatus.value = null
        }
    }

    fun downloadAllReciterAudio(reciterId: String) {
        if (_downloadingSurahNumber.value != null || _isDownloadingAllTexts.value) {
            _downloadStatus.value = "هناك عملية تحميل أخرى قيد التشغيل حالياً."
            return
        }
        val freeBytes = com.example.util.QuranStorageManager.getStorageFreeSpace(getApplication(), _useSdCardForDownloads.value)
        if (freeBytes > 0 && freeBytes < 150 * 1024 * 1024) {
            android.widget.Toast.makeText(getApplication(), "تحذير: المساحة المتاحة أقل من 150 ميجابايت، قد تنفد المساحة أثناء تحميل المصحف الصوتي كاملاً.", android.widget.Toast.LENGTH_LONG).show()
        }
        viewModelScope.launch {
            _downloadingSurahNumber.value = -1
            _downloadProgress.value = 0f
            _downloadStatus.value = "جاري بدء تحميل المصحف الصوتي كاملاً..."

            val list = _allSurahs.value.ifEmpty {
                val listResult = quranRepo.getSurahsList()
                listResult.getOrDefault(emptyList())
            }
            if (list.isEmpty()) {
                _downloadStatus.value = "فشل بدء التحميل: تعذر جلب قائمة السور."
                _downloadingSurahNumber.value = null
                return@launch
            }

            val folder = com.example.util.RECITER_FOLDER_MAP[reciterId] ?: "Alafasy_128kbps"
            val totalSurahs = list.size
            var completedCount = 0

            for ((index, surah) in list.withIndex()) {
                val surahNum = surah.number
                val totalAyahs = surah.numberOfAyahs
                for (i in 1..totalAyahs) {
                    _downloadStatus.value = "تحميل سورة ${surah.name} (آية $i من $totalAyahs) - [سورة ${index + 1} من $totalSurahs]"
                    com.example.util.QuranStorageManager.downloadAyah(
                        getApplication(),
                        _useSdCardForDownloads.value,
                        folder,
                        reciterId,
                        surahNum,
                        i
                    )
                }
                completedCount++
                _downloadProgress.value = completedCount.toFloat() / totalSurahs
            }

            _downloadStatus.value = "تم تحميل المصحف الصوتي كاملاً بنجاح!"
            refreshDownloadedSurahs()
            kotlinx.coroutines.delay(4000)
            _downloadingSurahNumber.value = null
            _downloadProgress.value = 0f
            _downloadStatus.value = null
        }
    }
}
