package com.example.ui.components

import android.net.Uri
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import com.example.util.PrayerAutoSilentManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AdhanSound
import com.example.data.model.PRESET_ADHAN_SOUNDS
import com.example.ui.theme.CairoFontFamily
import com.example.util.AdhanAudioPlayer

@Composable
fun AdhanSettingsDialog(
    selectedSoundId: String,
    selectedFajrSoundId: String,
    customSoundUri: String?,
    customSoundName: String?,
    overrideSilentMode: Boolean = true,
    onToggleOverrideSilentMode: (Boolean) -> Unit = {},
    useFlipToMute: Boolean = true,
    onToggleFlipToMute: (Boolean) -> Unit = {},
    adhanAudioStreamType: String = "media",
    onSetAdhanAudioStreamType: (String) -> Unit = {},
    appLanguage: String = "ar",
    onDismiss: () -> Unit,
    onSaveSound: (soundId: String, fajrSoundId: String, customUri: String?, customName: String?) -> Unit
) {
    val context = LocalContext.current
    val audioPlayer = remember { AdhanAudioPlayer(context) }

    var currentSelectedId by remember { mutableStateOf(selectedSoundId) }
    var currentFajrSelectedId by remember { mutableStateOf(selectedFajrSoundId) }
    var isFajrTabSelected by remember { mutableStateOf(false) }
    var activeCustomUri by remember { mutableStateOf(customSoundUri) }
    var activeCustomName by remember { mutableStateOf(customSoundName) }



    var playingUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.stopSound()
        }
    }

    // Storage Audio File Picker Launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriString = uri.toString()
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "صوت مخصص من الجهاز"
            activeCustomUri = uriString
            activeCustomName = fileName
            currentSelectedId = "custom_local"
        }
    }

    val isAr = appLanguage != "en"

    Dialog(onDismissRequest = onDismiss) {
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
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "صوت الأذان",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_adhan_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = if (isAr) "الأصوات المتاحة:" else "Available Sounds:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Silent Mode Override Toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isAr) "تشغيل الأذان في الوضع الصامت" else "Play Adhan in Silent Mode",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAr) "انطلاق صوت الأذان حتى إذا كان الهاتف صامتاً" else "Play sound even when device is on silent/vibrate",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = overrideSilentMode,
                        onCheckedChange = { onToggleOverrideSilentMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                // Flip-to-Mute toggle row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isAr) "إيقاف الأذان بقلب الهاتف" else "Flip to stop Adhan",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAr) "اقلب شاشة الهاتف للأسفل لكتم الأذان" else "Turn phone face down to silence Adhan",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = useFlipToMute,
                        onCheckedChange = { onToggleFlipToMute(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                // Volume Stream Selection (Media vs Alarm)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isAr) "قناة التحكم بمستوى صوت الأذان" else "Adhan Volume Control Stream",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (adhanAudioStreamType == "alarm") {
                                    if (isAr) "يتبع صوت المنبه وساعة الإيقاظ (Alarm)" else "Controlled via Alarm volume"
                                } else {
                                    if (isAr) "يتبع صوت الوسائط والموسيقى (Media)" else "Controlled via Media volume"
                                },
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Segmented selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isMedia = adhanAudioStreamType != "alarm"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isMedia) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onSetAdhanAudioStreamType("media") }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAr) "صوت الوسائط (Media)" else "Media Volume",
                                color = if (isMedia) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (!isMedia) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onSetAdhanAudioStreamType("alarm") }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAr) "صوت المنبه (Alarm)" else "Alarm Volume",
                                color = if (!isMedia) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Prayer Auto-Silent Section Card
                PrayerAutoSilentConfigSection(
                    isAr = isAr,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "اختر الأذان المفضل لديك أو أضف ملف صوتي من ذاكرة جهازك:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Custom Device Storage Sound Option Button
                OutlinedButton(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_pick_custom_audio"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (activeCustomName != null) "ملف الجهاز: $activeCustomName" else "اختيار ملف صوتي من وحدة التخزين...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Preset Sounds List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    // Custom file item if loaded
                    if (activeCustomUri != null) {
                        item {
                            val isSelected = currentSelectedId == "custom_local"
                            AdhanSoundRowItem(
                                name = activeCustomName ?: "ملف صوتي مخصص",
                                subtitle = if (isAr) "ملف صوتي مخصص من ذاكرة الجهاز" else "Custom audio file",
                                isSelected = isSelected,
                                isPlayingStandard = playingUrl == activeCustomUri,
                                onClickSelect = {
                                    currentSelectedId = "custom_local"
                                    currentFajrSelectedId = "custom_local"
                                },
                                onTogglePlayStandard = {
                                    if (playingUrl == activeCustomUri) {
                                        audioPlayer.stopSound()
                                        playingUrl = null
                                    } else {
                                        audioPlayer.playSound(activeCustomUri!!) {
                                            playingUrl = null
                                        }
                                        playingUrl = activeCustomUri
                                    }
                                }
                            )
                        }
                    }

                    items(PRESET_ADHAN_SOUNDS) { sound ->
                        val isSelected = currentSelectedId == sound.id
                        AdhanSoundRowItem(
                            name = sound.nameAr,
                            subtitle = if (isAr) "أذان الصلوات الأربع + أذان الفجر بالتثويب" else "Standard 4 prayers + Authentic Fajr with Tathweeb",
                            isSelected = isSelected,
                            isPlayingStandard = playingUrl == sound.audioUrl,
                            isPlayingFajr = playingUrl == sound.fajrAudioUrl,
                            onClickSelect = {
                                currentSelectedId = sound.id
                                currentFajrSelectedId = sound.id
                            },
                            onTogglePlayStandard = {
                                if (playingUrl == sound.audioUrl) {
                                    audioPlayer.stopSound()
                                    playingUrl = null
                                } else {
                                    audioPlayer.playSound(sound.audioUrl) {
                                        playingUrl = null
                                    }
                                    playingUrl = sound.audioUrl
                                }
                            },
                            onTogglePlayFajr = {
                                if (playingUrl == sound.fajrAudioUrl) {
                                    audioPlayer.stopSound()
                                    playingUrl = null
                                } else {
                                    audioPlayer.playSound(sound.fajrAudioUrl) {
                                        playingUrl = null
                                    }
                                    playingUrl = sound.fajrAudioUrl
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Save Button
                Button(
                    onClick = {
                        audioPlayer.stopSound()
                        onSaveSound(currentSelectedId, currentFajrSelectedId, activeCustomUri, activeCustomName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_adhan_sound"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "اعتماد صوت الأذان",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
}
}

@Composable
private fun AdhanSoundRowItem(
    name: String,
    subtitle: String? = null,
    isSelected: Boolean,
    isPlayingStandard: Boolean = false,
    isPlayingFajr: Boolean = false,
    onClickSelect: () -> Unit,
    onTogglePlayStandard: () -> Unit,
    onTogglePlayFajr: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClickSelect)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Dual Preview Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Standard adhan preview
                IconButton(
                    onClick = onTogglePlayStandard,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isPlayingStandard) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = if (isPlayingStandard) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "معاينة الأذان العام",
                        tint = if (isPlayingStandard) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Fajr adhan preview (if available)
                if (onTogglePlayFajr != null) {
                    IconButton(
                        onClick = onTogglePlayFajr,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isPlayingFajr) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = if (isPlayingFajr) "⏹" else "🌙",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
