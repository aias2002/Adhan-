package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.example.R
import com.example.ui.theme.CairoFontFamily
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.BookmarkEntity
import com.example.data.model.AyahItem
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import com.example.util.PRESET_RECITERS

val KfgqpcHafsFontFamily = FontFamily(
    Font(R.font.kfgqpc_hafs, FontWeight.Normal),
    Font(R.font.amiri_quran, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.cairo_semibold, FontWeight.Normal)
)

val KfgqpcWarshFontFamily = FontFamily(
    Font(R.font.amiri_quran, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.cairo_semibold, FontWeight.Normal)
)

private fun toEasternArabicDigits(number: Int, numeralSystem: String = "western"): String {
    return com.example.ui.util.AppLocalization.formatNumber(number, numeralSystem)
}

@Composable
fun QuranScreen(
    quranEdition: String,
    quranFontSize: Float,
    quranFontType: String,
    selectedReciterId: String,
    startAyah: Int,
    endAyah: Int,
    repeatPerAyah: Int,
    playingAyahNumber: Int?,
    isPlayingQuranAudio: Boolean,
    isQuranAudioPaused: Boolean = false,
    playbackProgressRatio: Float = 0f,
    surahs: List<SurahItem>,
    searchQuery: String,
    selectedSurah: SurahDetail?,
    targetScrollAyah: Int? = null,
    onClearTargetScrollAyah: () -> Unit = {},
    bookmark: BookmarkEntity?,
    isLoading: Boolean,
    errorMessage: String?,
    useSdCardForDownloads: Boolean = false,
    downloadingSurahNumber: Int? = null,
    downloadProgress: Float = 0f,
    downloadStatus: String? = null,
    isDownloadingAllTexts: Boolean = false,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    onSetUseSdCardForDownloads: (Boolean) -> Unit = {},
    onDownloadSurah: (surahNumber: Int, reciterId: String, totalAyahs: Int) -> Unit = { _, _, _ -> },
    onDownloadAllTexts: () -> Unit = {},
    onEditionChanged: (String) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onFontTypeChanged: (String) -> Unit,
    onReciterChanged: (String) -> Unit,
    onRangeChanged: (start: Int, end: Int) -> Unit,
    onRepeatCountChanged: (Int) -> Unit,
    onTogglePlayAudio: (targetAyah: Int?) -> Unit,
    onPauseAudio: () -> Unit = {},
    onResumeAudio: () -> Unit = {},
    onStopAudio: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSurahSelected: (Int, Int?) -> Unit,
    onCloseSurahView: () -> Unit,
    onSaveBookmark: (surahNumber: Int, surahName: String, ayahNumber: Int) -> Unit,
    recitationNetworkError: String? = null,
    onDismissRecitationError: () -> Unit = {},
    downloadedSurahs: Set<Int> = emptySet(),
    appLanguage: String = "ar",
    onDownloadAllAudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDownloadManagerDialog by remember { mutableStateOf(false) }
    val isAr = appLanguage != "en"

    // Keep screen on while QuranScreen is active (reading or playing audio)
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Box(modifier = modifier.fillMaxSize()) {
            if (selectedSurah != null) {
                // Surah Detail Reading View
                SurahDetailView(
                    surah = selectedSurah,
                    targetScrollAyah = targetScrollAyah,
                    onClearTargetScrollAyah = onClearTargetScrollAyah,
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
                    bookmark = bookmark,
                    isFullScreen = isFullScreen,
                    onToggleFullScreen = onToggleFullScreen,
                    downloadingSurahNumber = downloadingSurahNumber,
                    downloadProgress = downloadProgress,
                    downloadStatus = downloadStatus,
                    onDownloadSurah = onDownloadSurah,
                    recitationNetworkError = recitationNetworkError,
                    onDismissRecitationError = onDismissRecitationError,
                    onFontSizeChanged = onFontSizeChanged,
                    onFontTypeChanged = onFontTypeChanged,
                    onReciterChanged = onReciterChanged,
                    onRangeChanged = onRangeChanged,
                    onRepeatCountChanged = onRepeatCountChanged,
                    onTogglePlayAudio = onTogglePlayAudio,
                    onPauseAudio = onPauseAudio,
                    onResumeAudio = onResumeAudio,
                    onStopAudio = onStopAudio,
                    onBack = onCloseSurahView,
                    onSaveBookmark = { ayahNum ->
                        onSaveBookmark(selectedSurah.number, selectedSurah.name, ayahNum)
                    }
                )
            } else {
                // Surahs Catalog List View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 700.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Unified Control Bar (Riwaya Selector)
                    RiwayaEditionBar(
                        currentEdition = quranEdition,
                        onEditionChanged = onEditionChanged,
                        appLanguage = appLanguage
                    )

                    // Golden Bookmark Continue Banner (If present)
                    if (bookmark != null) {
                        BookmarkContinueBanner(
                            bookmark = bookmark,
                            onContinue = { onSurahSelected(bookmark.surahNumber, bookmark.ayahNumber) },
                            appLanguage = appLanguage
                        )
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text(if (isAr) stringResource(R.string.search_hint) else "Search by name or number…", fontSize = 13.5.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (isAr) "مسح البحث" else "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_quran_search"),
                        shape = RoundedCornerShape(20.dp)
                    )

                    // Surahs Grid List with Smart Arabic Normalization Filter
                    val filteredSurahs = remember(surahs, searchQuery, appLanguage) {
                        if (searchQuery.isBlank()) {
                            surahs
                        } else {
                            surahs.filter { matchesSurahSearch(it, searchQuery) }
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = if (isAr) "جاري تحميل المصحف الشريف..." else "Loading Holy Quran...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else if (!errorMessage.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(onClick = { if (selectedSurah != null) onSurahSelected(selectedSurah.number, null) }) {
                                        Text(if (isAr) "إعادة المحاولة" else "Try again")
                                    }
                                    Button(
                                        onClick = { onEditionChanged("quran-uthmani") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text(stringResource(R.string.return_to_hafs))
                                    }
                                }
                            }
                        }
                    } else if (filteredSurahs.isEmpty() && searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = if (isAr) "لا توجد سورة تطابق: \"$searchQuery\"" else "No surahs found",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = CairoFontFamily
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredSurahs,
                                key = { it.number }
                            ) { surah ->
                                val isSurahDownloaded = downloadedSurahs.contains(surah.number)
                                SurahCardItem(
                                    surah = surah,
                                    selectedReciterId = selectedReciterId,
                                    isDownloaded = isSurahDownloaded,
                                    isDownloading = downloadingSurahNumber == surah.number,
                                    onDownload = { onDownloadSurah(surah.number, selectedReciterId, surah.numberOfAyahs) },
                                    onClick = { onSurahSelected(surah.number, null) },
                                    appLanguage = appLanguage
                                )
                            }
                        }
                    }
                }
            }

            if (showDownloadManagerDialog) {
                DownloadManagerDialog(
                    useSdCardForDownloads = useSdCardForDownloads,
                    downloadingSurahNumber = downloadingSurahNumber,
                    downloadProgress = downloadProgress,
                    downloadStatus = downloadStatus,
                    isDownloadingAllTexts = isDownloadingAllTexts,
                    onSetUseSdCardForDownloads = onSetUseSdCardForDownloads,
                    onDownloadAllTexts = onDownloadAllTexts,
                    onDismiss = { showDownloadManagerDialog = false }
                )
            }
        }
    }
}

@Composable
private fun QuranAudioPlayerControlCard(
    totalAyahs: Int,
    selectedReciterId: String,
    startAyah: Int,
    endAyah: Int,
    repeatPerAyah: Int,
    playingAyahNumber: Int?,
    isPlayingQuranAudio: Boolean,
    isQuranAudioPaused: Boolean = false,
    surahNumber: Int = 1,
    downloadingSurahNumber: Int? = null,
    downloadProgress: Float = 0f,
    downloadStatus: String? = null,
    onDownloadSurah: (Int, String, Int) -> Unit = { _, _, _ -> },
    onReciterChanged: (String) -> Unit,
    onRangeChanged: (start: Int, end: Int) -> Unit,
    onRepeatCountChanged: (Int) -> Unit,
    onTogglePlayAudio: (targetAyah: Int?) -> Unit,
    onPauseAudio: () -> Unit = {},
    onResumeAudio: () -> Unit = {},
    onStopAudio: () -> Unit,
    onClose: () -> Unit = {}
) {
    var showReciterDialog by remember { mutableStateOf(false) }
    var expandedPlayer by remember { mutableStateOf(true) }

    val reciterName = remember(selectedReciterId) {
        PRESET_RECITERS.find { it.id == selectedReciterId }?.nameAr ?: "مشاري العفاسي"
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isPlayingQuranAudio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Right Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .testTag("btn_close_quran_audio_card")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "غلق البطاقة",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clickable { showReciterDialog = true }
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (playingAyahNumber != null) "$reciterName • آية ($playingAyahNumber)" else reciterName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "✏️",
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (!isPlayingQuranAudio) {
                        Button(
                            onClick = { onTogglePlayAudio(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_toggle_quran_audio")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "تشغيل",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Pause / Resume Button
                        Button(
                            onClick = {
                                if (isQuranAudioPaused) onResumeAudio() else onPauseAudio()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isQuranAudioPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_pause_resume_quran_audio")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isQuranAudioPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isQuranAudioPaused) "استئناف" else "مؤقت",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Stop Button
                        Button(
                            onClick = onStopAudio,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_stop_quran_audio")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "إيقاف",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Repeat & Range Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Range Selector (من الآية X إلى Y)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "الآيات ($startAyah - $endAyah):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                if (endAyah < totalAyahs) {
                                    onRangeChanged(1, totalAyahs)
                                } else {
                                    onRangeChanged(1, 5.coerceAtMost(totalAyahs))
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (endAyah == totalAyahs) "الكل" else "5 آيات",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Repeat Mode Selector (تكرار الآية)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "تكرار",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    listOf(1 to "1x", 3 to "3x", 5 to "5x", 999 to "∞").forEach { (repeatVal, labelStr) ->
                        val isSelected = repeatPerAyah == repeatVal
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .clickable { onRepeatCountChanged(repeatVal) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = labelStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Download Surah Button
            val isThisDownloading = downloadingSurahNumber == surahNumber
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onDownloadSurah(surahNumber, selectedReciterId, totalAyahs) },
                    enabled = downloadingSurahNumber == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isThisDownloading) "جاري التحميل..." else "📥 تحميل السورة صوتياً",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isThisDownloading && downloadStatus != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = downloadStatus,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
        }
    }

    if (showReciterDialog) {
        ReciterSelectorDialog(
            selectedReciterId = selectedReciterId,
            onDismiss = { showReciterDialog = false },
            onSelectReciter = { reciterId ->
                onReciterChanged(reciterId)
                showReciterDialog = false
            }
        )
    }
}

@Composable
private fun ReciterSelectorDialog(
    selectedReciterId: String,
    onDismiss: () -> Unit,
    onSelectReciter: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "اختر القارئ المفضل",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                ) {
                    items(PRESET_RECITERS) { reciter ->
                        val isSelected = selectedReciterId == reciter.id
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectReciter(reciter.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = reciter.nameAr,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = reciter.styleAr,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
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

@Composable
private fun QuranFontSettingsCard(
    fontSize: Float,
    onFontSizeChanged: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "حجم خط الآيات الكريمة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (expanded) "إخفاء ▲" else "تعديل الحجم (${fontSize.toInt()}sp) ▼",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحجم الحالي: ${fontSize.toInt()}sp",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { onFontSizeChanged((fontSize - 2).coerceAtLeast(18f)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { onFontSizeChanged((fontSize + 2).coerceAtMost(60f)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChanged,
                        valueRange = 18f..60f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun RiwayaEditionBar(
    currentEdition: String,
    onEditionChanged: (String) -> Unit,
    appLanguage: String = "ar"
) {
    val isAr = appLanguage != "en"
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isHafs = currentEdition == "quran-uthmani"
            val isWarsh = currentEdition == "quran-warsh"

            // Segmented Riwaya selector
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isHafs) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onEditionChanged("quran-uthmani") }
                    .padding(vertical = 11.dp)
                    .testTag("btn_riwaya_hafs"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "حفص عن عاصم" else "Hafs 'an 'Asim",
                    color = if (isHafs) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isWarsh) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onEditionChanged("quran-warsh") }
                    .padding(vertical = 11.dp)
                    .testTag("btn_riwaya_warsh"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "ورش عن نافع" else "Warsh 'an Nafi'",
                    color = if (isWarsh) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@Composable
private fun BookmarkContinueBanner(
    bookmark: BookmarkEntity,
    onContinue: () -> Unit,
    appLanguage: String = "ar"
) {
    val isAr = appLanguage != "en"
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Color(0xFFFFD700).copy(alpha = 0.6f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onContinue)
            .testTag("banner_continue_bookmark")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(22.dp)
                    )
                }
                val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                Column {
                    Text(
                        text = if (isAr) "متابعة القراءة المرجعية 🔖" else "Continue Reading 🔖",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    val surahDisplayName = if (isAr) {
                        bookmark.surahName
                    } else {
                        com.example.ui.util.AppLocalization.getSurahEnglishName(bookmark.surahNumber)
                    }
                    val ayahWord = if (isAr) "آية" else "Ayah"
                    Text(
                        text = "$surahDisplayName · $ayahWord ${toEasternArabicDigits(bookmark.ayahNumber, numeralSystem)}",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isAr) "متابعة ←" else "Continue →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SurahCardItem(
    surah: SurahItem,
    selectedReciterId: String,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onClick: () -> Unit,
    appLanguage: String
) {
    val cleanName = remember(surah.name) { cleanSurahDisplayName(surah.name) }
    val isMeccan = remember(surah.revelationType) { surah.revelationType.equals("Meccan", true) }

    val isAr = appLanguage != "en"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .testTag("surah_item_${surah.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Illuminated Surah Number Star Frame
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                    Text(
                        text = toEasternArabicDigits(surah.number, numeralSystem),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
                    val surahName = if (isAr) "سُورَةُ $cleanName" else com.example.ui.util.AppLocalization.getSurahEnglishName(surah.number)
                    val surahMeaning = if (isAr) "" else "(${com.example.ui.util.AppLocalization.getSurahEnglishMeaning(surah.number)})"
                    
                    Text(
                        text = surahName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val versesCount = surah.numberOfAyahs
                    val versesText = if (isAr) {
                        "${toEasternArabicDigits(versesCount, numeralSystem)} آية"
                    } else {
                        if (versesCount == 1) "1 verse" else "$versesCount verses"
                    }
                    val secondLine = if (isAr) {
                        "${surah.englishName} · $versesText"
                    } else {
                        "$surahMeaning · $versesText"
                    }
                    
                    Text(
                        text = secondLine,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Revelation Badge (Meccan / Medinan)
                Text(
                    text = if (isAr) {
                        if (isMeccan) "مكّية 🌙" else "مدنيّة 🕌"
                    } else {
                        if (isMeccan) "Meccan 🌙" else "Medinan 🕌"
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                // Download Status or Button
                IconButton(
                    onClick = onDownload,
                    enabled = !isDownloading,
                    modifier = Modifier.size(34.dp)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = if (isAr) (if (isDownloaded) "متوفر بدون إنترنت" else "غير متوفر بدون إنترنت") else "Download status",
                            tint = if (isDownloaded) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun cleanSurahDisplayName(name: String): String {
    // Strips any duplicate or repeated "سورة" / "سُورَةُ" prefixes
    return name.replace(Regex("^(?:س[َُِ]?و[ْ]?ر[َُِ]?ة[َُِ]?\\s*)+"), "").trim()
}

@Composable
private fun SurahDetailView(
    surah: SurahDetail,
    targetScrollAyah: Int? = null,
    onClearTargetScrollAyah: () -> Unit = {},
    quranEdition: String,
    quranFontSize: Float,
    quranFontType: String,
    selectedReciterId: String,
    startAyah: Int,
    endAyah: Int,
    repeatPerAyah: Int,
    playingAyahNumber: Int?,
    isPlayingQuranAudio: Boolean,
    isQuranAudioPaused: Boolean = false,
    playbackProgressRatio: Float = 0f,
    bookmark: BookmarkEntity?,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    onFontSizeChanged: (Float) -> Unit,
    onFontTypeChanged: (String) -> Unit,
    onReciterChanged: (String) -> Unit,
    onRangeChanged: (start: Int, end: Int) -> Unit,
    onRepeatCountChanged: (Int) -> Unit,
    onTogglePlayAudio: (targetAyah: Int?) -> Unit,
    onPauseAudio: () -> Unit = {},
    onResumeAudio: () -> Unit = {},
    onStopAudio: () -> Unit,
    onBack: () -> Unit,
    onSaveBookmark: (ayahNumber: Int) -> Unit,
    recitationNetworkError: String? = null,
    onDismissRecitationError: () -> Unit = {},
    downloadingSurahNumber: Int? = null,
    downloadProgress: Float = 0f,
    downloadStatus: String? = null,
    onDownloadSurah: (Int, String, Int) -> Unit = { _, _, _ -> }
) {
    var showPlayerControls by remember { mutableStateOf(false) }
    var selectedAyahForAction by remember { mutableStateOf<AyahItem?>(null) }
    val listState = rememberLazyListState()

    val pureSurahName = remember(surah.name) {
        cleanSurahDisplayName(surah.name)
    }

    val selectedFontFamily = if (quranEdition == "quran-warsh") KfgqpcWarshFontFamily else KfgqpcHafsFontFamily

    // Colors matching user specification:
    // Page background: #010818 (very dark navy, almost black, no gradients or borders)
    // Text color: warm gold #F0C95A
    // Verse medallion & number: same warm gold #F0C95A
    val darkNavyBg = Color(0xFF010818)
    val quranTextColors = listOf(
        Color(0xFFF0C95A), // Gold
        Color(0xFFFFFFFF), // White
        Color(0xFF10B981), // Green
        Color(0xFF38BDF8), // Cyan
        Color(0xFFF472B6)  // Rose
    )
    val context = LocalContext.current
    val quranPrefs = remember { context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE) }
    var textColorIndex by remember {
        mutableStateOf(quranPrefs.getInt("quran_text_color_index", 0).coerceIn(0, quranTextColors.size - 1))
    }
    val currentQuranTextColor = quranTextColors[textColorIndex % quranTextColors.size]

    val onCycleTextColor: () -> Unit = {
        val next = (textColorIndex + 1) % quranTextColors.size
        textColorIndex = next
        quranPrefs.edit().putInt("quran_text_color_index", next).apply()
    }

    val quranGoldColor = currentQuranTextColor
    val ayahMedallionColor = currentQuranTextColor
    val pageSeparatorColor = currentQuranTextColor.copy(alpha = 0.35f)
    val bismillahColor = currentQuranTextColor

    val effectiveFontSize = remember(quranFontSize) {
        quranFontSize.coerceIn(18f, 44f)
    }

    // Group ayahs by page
    val pagesMap = remember(surah.ayahs) {
        val map = linkedMapOf<Int, MutableList<AyahItem>>()
        surah.ayahs.forEachIndexed { _, ayah ->
            val pageNum = ayah.page ?: ((ayah.number / 8) + 1)
            map.getOrPut(pageNum) { mutableListOf() }.add(ayah)
        }
        map
    }

    val firstAyah = surah.ayahs.firstOrNull()
    val currentJuz = firstAyah?.juz ?: 1
    val currentHizb = ((currentJuz - 1) * 2) + 1

    var isAutoScrolling by remember { androidx.compose.runtime.mutableStateOf(false) }

    // Scroll to target/bookmarked Ayah automatically
    val targetAyah = targetScrollAyah ?: if (bookmark?.surahNumber == surah.number) bookmark.ayahNumber else null
    androidx.compose.runtime.LaunchedEffect(targetAyah, pagesMap) {
        if (targetAyah != null && pagesMap.isNotEmpty()) {
            val pageIdx = pagesMap.entries.indexOfFirst { entry ->
                entry.value.any { it.numberInSurah == targetAyah }
            }
            if (pageIdx >= 0) {
                val itemIndex = if (pageIdx == 0) 1 else (pageIdx * 2 + 1)
                listState.animateScrollToItem(itemIndex)
                onClearTargetScrollAyah()
            }
        }
    }

    var userScrolledManually by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && isPlayingQuranAudio) {
            userScrolledManually = true
        }
    }

    val centerActiveAyah: suspend () -> Unit = {
        if (playingAyahNumber != null && pagesMap.isNotEmpty()) {
            val pageEntry = pagesMap.entries.firstOrNull { entry ->
                entry.value.any { it.numberInSurah == playingAyahNumber }
            }
            if (pageEntry != null) {
                val pageIdx = pagesMap.keys.indexOf(pageEntry.key)
                val ayahsInPage = pageEntry.value
                val ayahIdxInPage = ayahsInPage.indexOfFirst { it.numberInSurah == playingAyahNumber }
                val pageBlockItemIndex = if (pageIdx == 0) 1 else (pageIdx * 2 + 1)

                val fraction = if (ayahsInPage.isNotEmpty()) ayahIdxInPage.toFloat() / ayahsInPage.size else 0f
                val offset = (fraction * 450).toInt()
                listState.animateScrollToItem(pageBlockItemIndex, offset)
            }
        }
    }

    // Synchronize screen scroll with recitation playback automatically and keep active verse centered and in focus
    androidx.compose.runtime.LaunchedEffect(playingAyahNumber, isPlayingQuranAudio) {
        if (isPlayingQuranAudio && playingAyahNumber != null && !isAutoScrolling && pagesMap.isNotEmpty()) {
            userScrolledManually = false
            centerActiveAyah()
        }
    }

    if (isAutoScrolling) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (isActive) {
                try {
                    listState.scrollBy(1.2f)
                } catch (e: Exception) {
                    // ignore listState manipulation errors during manual drag or transitions
                }
                kotlinx.coroutines.delay(25)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkNavyBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_back_to_surahs").size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع للفهرس",
                        tint = quranGoldColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "سُورَةُ $pureSurahName",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = quranGoldColor,
                        fontFamily = CairoFontFamily,
                        maxLines = 1
                    )
                    Text(
                        text = if (quranEdition == "quran-warsh") stringResource(R.string.warsh_short) else stringResource(R.string.hafs_short),
                        fontSize = 11.sp,
                        color = quranGoldColor.copy(alpha = 0.65f),
                        fontFamily = CairoFontFamily
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Size Toggle Button inside Surah Reading View
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(quranGoldColor.copy(alpha = 0.15f))
                            .clickable {
                                val next = when {
                                    quranFontSize < 24f -> 26f
                                    quranFontSize < 28f -> 30f
                                    quranFontSize < 34f -> 38f
                                    else -> 22f
                                }
                                onFontSizeChanged(next)
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "تغيير حجم الخط",
                                tint = quranGoldColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${quranFontSize.toInt()}sp",
                                color = quranGoldColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Audio Controls Toggle Button
                    IconButton(
                        onClick = { showPlayerControls = !showPlayerControls },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "مشغل التلاوة",
                            tint = if (isPlayingQuranAudio) Color(0xFF10B981) else quranGoldColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Fullscreen Toggle Button
                    IconButton(
                        onClick = onToggleFullScreen,
                        modifier = Modifier.size(38.dp).testTag("btn_toggle_fullscreen")
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullScreen) "الخروج من ملء الشاشة" else "عرض كامل الشاشة",
                            tint = if (isFullScreen) Color(0xFF10B981) else quranGoldColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quran Text Color Selector Circle Button
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(currentQuranTextColor)
                            .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                            .clickable { onCycleTextColor() }
                            .testTag("btn_quran_text_color")
                    )
                }
            }

            // Mini player floating bar whenever audio is playing and ribbon is hidden
            if (isPlayingQuranAudio && !showPlayerControls) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF030D20).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, quranGoldColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showPlayerControls = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            val reciterName = PRESET_RECITERS.find { it.id == selectedReciterId }?.nameAr ?: ""
                            Text(
                                text = "تلاوة: $reciterName ${if (playingAyahNumber != null) "• آية ($playingAyahNumber)" else ""}",
                                fontSize = 12.sp,
                                color = quranGoldColor,
                                fontFamily = CairoFontFamily
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (userScrolledManually) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .clickable {
                                            userScrolledManually = false
                                            coroutineScope.launch { centerActiveAyah() }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "🎯 تمركز",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (isQuranAudioPaused) onResumeAudio() else onPauseAudio()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isQuranAudioPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = quranGoldColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onStopAudio,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recitation Offline / Internet Connection Alert Notification Banner
            AnimatedVisibility(visible = recitationNetworkError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه عدم الاتصال بالإنترنت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = recitationNetworkError.orEmpty(),
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(
                            onClick = onDismissRecitationError,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Audio Player Controls (Expandable Ribbon)
            AnimatedVisibility(visible = showPlayerControls) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    QuranAudioPlayerControlCard(
                        totalAyahs = surah.ayahs.size,
                        selectedReciterId = selectedReciterId,
                        startAyah = startAyah,
                        endAyah = endAyah,
                        repeatPerAyah = repeatPerAyah,
                        playingAyahNumber = playingAyahNumber,
                        isPlayingQuranAudio = isPlayingQuranAudio,
                        isQuranAudioPaused = isQuranAudioPaused,
                        surahNumber = surah.number,
                        downloadingSurahNumber = downloadingSurahNumber,
                        downloadProgress = downloadProgress,
                        downloadStatus = downloadStatus,
                        onDownloadSurah = onDownloadSurah,
                        onReciterChanged = onReciterChanged,
                        onRangeChanged = onRangeChanged,
                        onRepeatCountChanged = onRepeatCountChanged,
                        onTogglePlayAudio = { targetAyah ->
                            onTogglePlayAudio(targetAyah)
                            showPlayerControls = false
                        },
                        onPauseAudio = onPauseAudio,
                        onResumeAudio = {
                            onResumeAudio()
                            showPlayerControls = false
                        },
                        onStopAudio = onStopAudio,
                        onClose = { showPlayerControls = false }
                    )
                }
            }

            // Continuous Mushaf Page Flow with Pinch-to-Zoom Gesture
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            if (zoom != 1f) {
                                val newScale = (quranFontSize * zoom).coerceIn(20f, 44f)
                                onFontSizeChanged(newScale)
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Surah Opening Header & Bismillah
                item(key = "surah_header") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                    ) {
                        SurahOrnateHeader(
                            surahName = pureSurahName,
                            quranFontSize = effectiveFontSize,
                            selectedFontFamily = selectedFontFamily,
                            showBismillah = surah.number != 1 && surah.number != 9,
                            bismillahColor = bismillahColor,
                            textColor = quranGoldColor
                        )
                    }
                }

                // Render each page with its ornate page separator
                pagesMap.entries.forEachIndexed { pageIdx, (pageNumber, ayahsInPage) ->
                    // Page Ornate Divider
                    item(key = "divider_$pageNumber") {
                        if (pageIdx > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 600.dp)
                            ) {
                                TahajjadPageDivider(
                                    pageNumber = pageNumber,
                                    dividerColor = pageSeparatorColor
                                )
                            }
                        }
                    }

                    // Continuous verses of this page directly on the #010818 background
                    item(key = "page_$pageNumber") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                        ) {
                            TahajjadPageBlock(
                                surahNumber = surah.number,
                                ayahs = ayahsInPage,
                                quranFontSize = effectiveFontSize,
                                quranTextColor = quranGoldColor,
                                medallionColor = ayahMedallionColor,
                                fontFamily = selectedFontFamily,
                                playingAyahNumber = playingAyahNumber,
                                playbackProgressRatio = playbackProgressRatio,
                                bookmarkedAyahNumber = if (bookmark?.surahNumber == surah.number) bookmark.ayahNumber else null,
                                onToggleAutoScroll = {
                                    isAutoScrolling = !isAutoScrolling
                                },
                                onAyahLongPressed = { ayah ->
                                    selectedAyahForAction = ayah
                                }
                            )
                        }
                    }
                }

                // Bottom padding for the floating bar
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(if (isFullScreen) 36.dp else 84.dp))
                }
            }
        }

        // Floating Bottom Mushaf Bar (Centered on dark navy background)
        if (!isFullScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TahajjadFloatingBottomBar(
                    surahName = pureSurahName,
                    juzNumber = currentJuz,
                    hizbNumber = currentHizb,
                    textColor = quranGoldColor,
                    onCycleTextColor = onCycleTextColor
                )
            }
        }

        // Elegant floating auto-scroll status pill/indicator
        if (isAutoScrolling) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFD700).copy(alpha = 0.20f))
                    .border(
                        1.dp,
                        Color(0xFFFFD700).copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { isAutoScrolling = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = quranGoldColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Ayah Action Dialog (Play recitation / Repeat / Bookmark)
        selectedAyahForAction?.let { selectedAyah ->
            Dialog(onDismissRequest = { selectedAyahForAction = null }) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF040E22)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, quranGoldColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp).padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Compact Ayah Number badge with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(quranGoldColor.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = quranGoldColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "الآية ﴿ ${toEasternArabicDigits(selectedAyah.numberInSurah)} ﴾",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = quranGoldColor,
                                fontFamily = CairoFontFamily
                            )
                        }

                        // Scrollable Ayah text container to prevent buttons clipping on long verses
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cleanAyahText(surah.number, selectedAyah.numberInSurah, selectedAyah.text),
                                fontSize = 19.sp,
                                fontFamily = selectedFontFamily,
                                color = quranGoldColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp
                            )
                        }

                        // Repeat Count Selector (تكرار الآية) with smart chips
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0A1B38).copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "تكرار",
                                tint = quranGoldColor,
                                modifier = Modifier.size(15.dp)
                            )
                            listOf(1 to "1x", 3 to "3x", 5 to "5x", 999 to "∞").forEach { (repeatVal, labelStr) ->
                                val isSelected = repeatPerAyah == repeatVal
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Color(0xFFD4AF37)
                                            else Color(0xFF040E22)
                                        )
                                        .clickable { onRepeatCountChanged(repeatVal) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = labelStr,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF010818) else quranGoldColor
                                    )
                                }
                            }
                        }

                        val isThisAyahPlaying = isPlayingQuranAudio && (playingAyahNumber == selectedAyah.numberInSurah)
                        val context = LocalContext.current

                        // Action Buttons: (Row 1: استماع/إيقاف + تكرار) - (Row 2: علامة + بحث غوغل)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. استماع أو إيقاف (Play / Stop)
                                if (isThisAyahPlaying) {
                                    Button(
                                        onClick = {
                                            onStopAudio()
                                            selectedAyahForAction = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFDC2626)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "إيقاف",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onTogglePlayAudio(selectedAyah.numberInSurah)
                                            selectedAyahForAction = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD4AF37)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color(0xFF010818),
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "استماع",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF010818),
                                            fontFamily = CairoFontFamily,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // 2. تكرار (Repeat playback) - disappears when playing
                                    Button(
                                        onClick = {
                                            if (repeatPerAyah == 1) {
                                                onRepeatCountChanged(3)
                                            }
                                            onTogglePlayAudio(selectedAyah.numberInSurah)
                                            selectedAyahForAction = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Repeat,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "تكرار",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 3. علامة (Bookmark)
                                Button(
                                    onClick = {
                                        onSaveBookmark(selectedAyah.numberInSurah)
                                        selectedAyahForAction = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0A1B38)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = quranGoldColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "علامة",
                                        fontWeight = FontWeight.Bold,
                                        color = quranGoldColor,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 13.sp
                                    )
                                }

                                // 4. بحث غوغل (Google Search)
                                Button(
                                    onClick = {
                                        val cleanedText = cleanAyahText(surah.number, selectedAyah.numberInSurah, selectedAyah.text)
                                        val query = "تفسير سورة ${pureSurahName} آية ${selectedAyah.numberInSurah} $cleanedText"
                                        val webSearchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                            putExtra(android.app.SearchManager.QUERY, query)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            setPackage("com.google.android.googlequicksearchbox")
                                        }
                                        try {
                                            context.startActivity(webSearchIntent)
                                        } catch (_: Exception) {
                                            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                                            val searchUrl = "https://www.google.com/search?q=$encodedQuery"
                                            
                                            // Try Google App first
                                            val googleAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                setPackage("com.google.android.googlequicksearchbox")
                                            }
                                            
                                            try {
                                                context.startActivity(googleAppIntent)
                                            } catch (_: Exception) {
                                                // Fallback to browser
                                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try {
                                                    context.startActivity(browserIntent)
                                                } catch (_: Exception) {
                                                    // Ignore if browser not found
                                                }
                                            }
                                        }
                                        selectedAyahForAction = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF132A54)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "بحث غوغل",
                                        tint = Color(0xFF93C5FD),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "بحث غوغل",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF93C5FD),
                                        fontFamily = CairoFontFamily,
                                        fontSize = 13.sp
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

@Composable
private fun SurahOrnateHeader(
    surahName: String,
    quranFontSize: Float,
    selectedFontFamily: FontFamily,
    showBismillah: Boolean,
    bismillahColor: Color,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Decorative Surah Frame on #010818 background
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF040E22))
                .border(
                    1.dp,
                    textColor.copy(alpha = 0.45f),
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "۞  سُورَةُ ${surahName.replace("سورة", "").trim()}  ۞",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = selectedFontFamily,
                color = textColor
            )
        }

        if (showBismillah) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = (quranFontSize * 1.05f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = selectedFontFamily,
                color = bismillahColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TahajjadPageDivider(
    pageNumber: Int,
    dividerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.8.dp)
                .background(dividerColor.copy(alpha = 0.25f))
        )

        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "۞",
                fontSize = 12.sp,
                color = dividerColor.copy(alpha = 0.7f)
            )
            Text(
                text = "الصفحة ${toEasternArabicDigits(pageNumber)}",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = CairoFontFamily,
                color = dividerColor.copy(alpha = 0.85f)
            )
            Text(
                text = "۞",
                fontSize = 12.sp,
                color = dividerColor.copy(alpha = 0.7f)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.8.dp)
                .background(dividerColor.copy(alpha = 0.25f))
        )
    }
}

@Composable
private fun TahajjadPageBlock(
    surahNumber: Int,
    ayahs: List<AyahItem>,
    quranFontSize: Float,
    quranTextColor: Color,
    medallionColor: Color,
    fontFamily: FontFamily,
    playingAyahNumber: Int?,
    playbackProgressRatio: Float = 0f,
    bookmarkedAyahNumber: Int?,
    onToggleAutoScroll: () -> Unit,
    onAyahLongPressed: (AyahItem) -> Unit
) {
    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
    val annotatedString = remember(ayahs, playingAyahNumber, bookmarkedAyahNumber, medallionColor, quranTextColor, numeralSystem, playingAyahNumber?.let { playbackProgressRatio }) {
        buildAnnotatedString {
            ayahs.forEachIndexed { index, ayah ->
                val isPlaying = playingAyahNumber == ayah.numberInSurah
                val isBookmarked = bookmarkedAyahNumber == ayah.numberInSurah
                val cleanText = cleanAyahText(surahNumber, ayah.numberInSurah, ayah.text).trim()
                val ayahNumStr = toEasternArabicDigits(ayah.numberInSurah, numeralSystem)

                pushStringAnnotation("AYAH", "${ayah.numberInSurah}")

                if (isPlaying) {
                    val words = cleanText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    val activeWordIdx = if (words.isNotEmpty()) {
                        (playbackProgressRatio * words.size).toInt().coerceIn(0, words.size - 1)
                    } else -1

                    words.forEachIndexed { wIdx, word ->
                        if (wIdx == activeWordIdx) {
                            pushStyle(
                                SpanStyle(
                                    color = Color(0xFFFACC15),
                                    background = Color(0xFF10B981).copy(alpha = 0.50f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            append(word)
                            pop()
                        } else {
                            pushStyle(
                                SpanStyle(
                                    color = Color(0xFFF0FDF4),
                                    background = Color(0xFF10B981).copy(alpha = 0.15f),
                                    fontWeight = FontWeight.Normal
                                )
                            )
                            append(word)
                            pop()
                        }
                        if (wIdx < words.size - 1) {
                            pushStyle(SpanStyle(background = Color(0xFF10B981).copy(alpha = 0.15f)))
                            append(" ")
                            pop()
                        }
                    }
                    append(" ")
                } else {
                    if (isBookmarked) {
                        pushStyle(SpanStyle(color = Color(0xFFFFF176), background = Color(0xFFF0C95A).copy(alpha = 0.20f)))
                    }
                    append(cleanText)
                    append(" ")
                    if (isBookmarked) {
                        pop()
                    }
                }

                // Clean verse number with selected numeral system
                pushStyle(
                    SpanStyle(
                        fontFamily = CairoFontFamily,
                        color = if (isPlaying) Color(0xFF10B981) else medallionColor.copy(alpha = 0.90f),
                        fontWeight = FontWeight.Bold
                    )
                )
                append("﴿$ayahNumStr﴾")
                pop()

                pop()

                if (index < ayahs.size - 1) {
                    append("  ")
                }
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotatedString,
            fontSize = quranFontSize.sp,
            fontFamily = fontFamily,
            lineHeight = (quranFontSize * 2.30f).sp,
            color = quranTextColor,
            textAlign = TextAlign.Start,
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(annotatedString) {
                    detectTapGestures(
                        onTap = {
                            onToggleAutoScroll()
                        },
                        onLongPress = { tapOffset ->
                            textLayoutResult?.let { layout ->
                                val offset = layout.getOffsetForPosition(tapOffset)
                                annotatedString.getStringAnnotations("AYAH", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        val num = annotation.item.toIntOrNull()
                                        ayahs.find { it.numberInSurah == num }?.let { clickedAyah ->
                                            onAyahLongPressed(clickedAyah)
                                        }
                                    }
                            }
                        }
                    )
                }
        )
    }
}

@Composable
private fun AyahEndMedallion(
    ayahNumber: Int,
    color: Color = Color(0xFFF0C95A),
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
    val easternNumber = remember(ayahNumber, numeralSystem) { toEasternArabicDigits(ayahNumber, numeralSystem) }

    Box(
        modifier = modifier
            .size(size)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 1.2.dp.toPx()
            val radius = (this.size.minDimension - strokeW) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Outer golden ring
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                style = Stroke(width = strokeW)
            )

            // Inner subtle ring
            drawCircle(
                color = color.copy(alpha = 0.50f),
                radius = radius * 0.80f,
                center = center,
                style = Stroke(width = 0.8.dp.toPx())
            )

            // 4 tiny ornate accents at top, bottom, left, right
            val dotRadius = 1.0.dp.toPx()
            val dotDist = radius * 0.80f
            drawCircle(color = color, radius = dotRadius, center = Offset(center.x, center.y - dotDist))
            drawCircle(color = color, radius = dotRadius, center = Offset(center.x, center.y + dotDist))
            drawCircle(color = color, radius = dotRadius, center = Offset(center.x - dotDist, center.y))
            drawCircle(color = color, radius = dotRadius, center = Offset(center.x + dotDist, center.y))
        }

        // Eastern Arabic numeral centered inside
        Text(
            text = easternNumber,
            color = color,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CairoFontFamily,
            textAlign = TextAlign.Center
        )
    }
}

private fun cleanAyahText(surahNumber: Int, ayahNumberInSurah: Int, rawText: String): String {
    var cleaned = rawText
        .replace(Regex("[\u06DD\u06DE\u06DF\u06E0\u06E2\u06E4\u06E5\u06E6\u06E7\u06E8\u06EA-\u06ED۝۞]"), "")
        .trim()

    if (surahNumber != 1 && surahNumber != 9 && ayahNumberInSurah == 1) {
        val bismillahPrefixes = listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيمِ",
            "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"
        )
        for (prefix in bismillahPrefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.removePrefix(prefix).trim()
                break
            }
        }
    }
    return if (cleaned.isNotBlank()) cleaned else rawText
}

@Composable
private fun TahajjadFloatingBottomBar(
    surahName: String,
    juzNumber: Int,
    hizbNumber: Int,
    textColor: Color,
    onCycleTextColor: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF030D20).copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            textColor.copy(alpha = 0.30f)
        ),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "السورة",
                    fontSize = 11.sp,
                    fontFamily = CairoFontFamily,
                    color = textColor.copy(alpha = 0.65f)
                )
                Text(
                    text = surahName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    color = textColor,
                    maxLines = 1
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "الجزء",
                    fontSize = 11.sp,
                    fontFamily = CairoFontFamily,
                    color = textColor.copy(alpha = 0.65f)
                )
                Text(
                    text = toEasternArabicDigits(juzNumber),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    color = textColor
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "الحزب",
                    fontSize = 11.sp,
                    fontFamily = CairoFontFamily,
                    color = textColor.copy(alpha = 0.65f)
                )
                Text(
                    text = toEasternArabicDigits(hizbNumber),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFontFamily,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun DownloadManagerDialog(
    useSdCardForDownloads: Boolean,
    downloadingSurahNumber: Int?,
    downloadProgress: Float,
    downloadStatus: String?,
    isDownloadingAllTexts: Boolean,
    onSetUseSdCardForDownloads: (Boolean) -> Unit,
    onDownloadAllTexts: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasSdCard = remember(context) { com.example.util.QuranStorageManager.hasSdCard(context) }
    
    val internalFree = remember(context) { com.example.util.QuranStorageManager.getStorageFreeSpace(context, false) / (1024 * 1024) }
    val internalTotal = remember(context) { com.example.util.QuranStorageManager.getStorageTotalSpace(context, false) / (1024 * 1024) }
    
    val sdFree = remember(context, hasSdCard) { 
        if (hasSdCard) com.example.util.QuranStorageManager.getStorageFreeSpace(context, true) / (1024 * 1024) else 0L
    }
    val sdTotal = remember(context, hasSdCard) { 
        if (hasSdCard) com.example.util.QuranStorageManager.getStorageTotalSpace(context, true) / (1024 * 1024) else 0L
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📥 إدارة التنزيلات والمساحة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // Storage Choice
                Text(
                    text = "مكان حفظ التنزيلات الصوتية:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Option 1: Internal Phone Storage
                    val isInternalSelected = !useSdCardForDownloads
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isInternalSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isInternalSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { onSetUseSdCardForDownloads(false) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isInternalSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "ذاكرة الهاتف الداخلية",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isInternalSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "المساحة المتاحة: $internalFree ميجابايت / $internalTotal ميجابايت",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Option 2: External SD Card Storage
                    val isSdSelected = useSdCardForDownloads
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSdSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSdSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable(enabled = hasSdCard) { onSetUseSdCardForDownloads(true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isSdSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (hasSdCard) "بطاقة الذاكرة الخارجية SD" else "بطاقة الذاكرة (غير مدرجة)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasSdCard) {
                                        if (isSdSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    } else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                if (hasSdCard) {
                                    Text(
                                        text = "المساحة المتاحة: $sdFree ميجابايت / $sdTotal ميجابايت",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "الرجاء إدخال بطاقة SD لتفعيل هذا الخيار",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Storage Path Hint
                val currentPath = remember(useSdCardForDownloads) {
                    com.example.util.QuranStorageManager.getAudioDirectory(context, useSdCardForDownloads)?.absolutePath ?: "تعذر تحديد المسار"
                }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "مسار الحفظ الحالي:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentPath,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                softWrap = true
                            )
                        }
                    }
                }

                // Download Quran Texts Offline Button
                Text(
                    text = "تنزيل المحتوى النصي أوفلاين:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onDownloadAllTexts,
                    enabled = !isDownloadingAllTexts && downloadingSurahNumber == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isDownloadingAllTexts) {
                        CircularProgressIndicator(
                            progress = downloadProgress,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.download_full_quran),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Active Download Progress UI
                if (downloadStatus != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = downloadStatus,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private val SURAH_SEARCH_ALIASES = mapOf(
    1 to listOf("فاتحه", "الفاتحه", "ام الكتاب", "السبع المثاني"),
    2 to listOf("بقره", "البقره"),
    9 to listOf("براءه", "براءة", "التوبه", "التوبة"),
    17 to listOf("بني اسرائيل", "الاسراء", "سبحان"),
    20 to listOf("طه"),
    36 to listOf("ياسين", "يس"),
    38 to listOf("صاد", "ص"),
    40 to listOf("المؤمن", "غافر"),
    50 to listOf("قاف", "ق"),
    68 to listOf("نون", "النون", "القلم"),
    76 to listOf("الدهر", "الانسان"),
    94 to listOf("الانشراح", "الشرح"),
    105 to listOf("الم تر", "الفيل"),
    108 to listOf("الكوثر", "انا اعطيناك"),
    109 to listOf("الكافرون", "قل يا ايها"),
    111 to listOf("تبت", "اللهب", "المسد"),
    112 to listOf("التوحيد", "الاخلاص", "قل هو الله احد"),
    113 to listOf("الفلق", "المعوذتين", "قل اعوذ برب الفلق"),
    114 to listOf("الناس", "المعوذتين", "قل اعوذ برب الناس")
)

private fun normalizeArabicSearch(text: String): String {
    if (text.isBlank()) return ""
    var s = text.trim()

    // 1. Convert Eastern Arabic numerals to Western
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    for (i in digits.indices) {
        s = s.replace(digits[i], ('0' + i))
    }

    // 2. Remove Quranic marks, Harakat / Tashkeel, Tatweel
    s = s.replace(Regex("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]"), "")

    // 3. Normalize all Alef forms (أ, إ, آ, ٱ -> ا)
    s = s.replace(Regex("[أإآٱ]"), "ا")

    // 4. Normalize Ta Marbuta (ة -> ه)
    s = s.replace('ة', 'ه')

    // 5. Normalize Alef Maksura (ى -> ي)
    s = s.replace('ى', 'ي')

    // 6. Normalize Hamza on Waw / Ya (ؤ -> و, ئ -> ي)
    s = s.replace('ؤ', 'و').replace('ئ', 'ي')

    // 7. Strip word prefix "سورة" / "سوره"
    s = s.replace(Regex("\\b(سورة|سوره)\\b"), "").trim()

    // 8. Collapse spaces
    s = s.replace(Regex("\\s+"), " ")

    return s.trim()
}

private fun stripAlPrefix(text: String): String {
    return if (text.startsWith("ال") && text.length > 2) {
        text.substring(2)
    } else {
        text
    }
}

private fun matchesSurahSearch(surah: SurahItem, rawQuery: String): Boolean {
    val cleanQuery = rawQuery.trim()
    if (cleanQuery.isEmpty()) return true

    val normQuery = normalizeArabicSearch(cleanQuery)
    if (surah.number.toString() == cleanQuery || surah.number.toString() == normQuery) {
        return true
    }

    if (surah.englishName.contains(cleanQuery, ignoreCase = true) ||
        surah.englishNameTranslation.contains(cleanQuery, ignoreCase = true)
    ) {
        return true
    }

    val normSurahName = normalizeArabicSearch(surah.name)
    val stemSurah = stripAlPrefix(normSurahName)
    val stemQuery = stripAlPrefix(normQuery)

    if (normSurahName.contains(normQuery) || stemSurah.contains(stemQuery) || stemSurah.contains(normQuery) || normSurahName.contains(stemQuery)) {
        return true
    }

    val aliases = SURAH_SEARCH_ALIASES[surah.number]
    if (aliases != null) {
        for (alias in aliases) {
            val normAlias = normalizeArabicSearch(alias)
            val stemAlias = stripAlPrefix(normAlias)
            if (normAlias.contains(normQuery) || stemAlias.contains(stemQuery) || normQuery.contains(stemAlias) || stemQuery.contains(stemAlias)) {
                return true
            }
        }
    }

    return false
}
