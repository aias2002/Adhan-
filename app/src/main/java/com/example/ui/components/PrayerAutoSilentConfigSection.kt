package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CairoFontFamily
import com.example.util.PrayerAutoSilentManager

data class PrayerSilentItemConfig(
    val key: String,
    val nameAr: String,
    val nameEn: String,
    val enabledPrefKey: String,
    val delayPrefKey: String,
    val durationPrefKey: String,
    val defaultDelay: Int,
    val defaultDuration: Int
)

val PRAYER_SILENT_CONFIGS = listOf(
    PrayerSilentItemConfig("fajr", "الفجر", "Fajr", PrayerAutoSilentManager.KEY_SILENT_FAJR, PrayerAutoSilentManager.KEY_DELAY_FAJR, PrayerAutoSilentManager.KEY_DURATION_FAJR, 15, 25),
    PrayerSilentItemConfig("dhuhr", "الظهر", "Dhuhr", PrayerAutoSilentManager.KEY_SILENT_DHUHR, PrayerAutoSilentManager.KEY_DELAY_DHUHR, PrayerAutoSilentManager.KEY_DURATION_DHUHR, 10, 20),
    PrayerSilentItemConfig("asr", "العصر", "Asr", PrayerAutoSilentManager.KEY_SILENT_ASR, PrayerAutoSilentManager.KEY_DELAY_ASR, PrayerAutoSilentManager.KEY_DURATION_ASR, 10, 20),
    PrayerSilentItemConfig("maghrib", "المغرب", "Maghrib", PrayerAutoSilentManager.KEY_SILENT_MAGHRIB, PrayerAutoSilentManager.KEY_DELAY_MAGHRIB, PrayerAutoSilentManager.KEY_DURATION_MAGHRIB, 5, 20),
    PrayerSilentItemConfig("isha", "العشاء", "Isha", PrayerAutoSilentManager.KEY_SILENT_ISHA, PrayerAutoSilentManager.KEY_DELAY_ISHA, PrayerAutoSilentManager.KEY_DURATION_ISHA, 10, 25),
    PrayerSilentItemConfig("jumuah", "صلاة الجمعة", "Jumu'ah", PrayerAutoSilentManager.KEY_SILENT_JUMUAH, PrayerAutoSilentManager.KEY_DELAY_JUMUAH, PrayerAutoSilentManager.KEY_DURATION_JUMUAH, 0, 45)
)

private enum class CustomValueType {
    DELAY,
    DURATION
}

private data class CustomDialogState(
    val prayerConfig: PrayerSilentItemConfig,
    val type: CustomValueType,
    val currentValue: Int
)

/**
 * Dedicated Auto-Silent Dialog compliant with strict proportional scaling (u = width / 340).
 * - Width = 92% of available screen width, max 420dp, horizontally centered.
 * - Height = wrap content, max 88% of screen height, no fixed height.
 * - Header and bottom button are fixed and visible. Middle content scrollable.
 * - LocalDensity locked to (density = 1f, fontScale = 1f) so Display Size and Font Size cannot distort layout.
 */
@Composable
fun PrayerAutoSilentDialog(
    isAr: Boolean = true,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit = onDismiss
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val systemDensity = LocalDensity.current
            val screenWidthDp = maxWidth
            val screenHeightDp = maxHeight

            // Width: 92% of available width up to 420.dp
            val targetWidthDp = minOf(screenWidthDp * 0.92f, 420.dp)
            val maxDialogHeightDp = screenHeightDp * 0.88f
            val u = (targetWidthDp.value / 340f).coerceIn(0.85f, 1.25f)

            // Touch target minimum 48dp
            val minTouchTargetDp = 48.dp

            CompositionLocalProvider(
                LocalDensity provides Density(density = systemDensity.density, fontScale = 1f),
                LocalLayoutDirection provides (if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr),
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = CairoFontFamily)
            ) {
                Surface(
                    shape = RoundedCornerShape((24 * u).dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = (6 * u).dp,
                    shadowElevation = (10 * u).dp,
                    border = BorderStroke((1 * u).dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .width(targetWidthDp)
                        .heightIn(max = maxDialogHeightDp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding((20 * u).dp)
                    ) {
                        // 1. Fixed Header (Icon, Title, Close X)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((10 * u).dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((38 * u).dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size((22 * u).dp)
                                    )
                                }
                                Text(
                                    text = if (isAr) "الوضع الصامت أثناء الصلاة 🔕" else "Prayer Auto-Silent Mode 🔕",
                                    fontSize = (20 * u).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            val touchSize = maxOf((44 * u).dp, minTouchTargetDp)
                            Box(
                                modifier = Modifier
                                    .size(touchSize)
                                    .clickable(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isAr) "إغلاق" else "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size((24 * u).dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height((12 * u).dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = (1 * u).dp
                        )
                        Spacer(modifier = Modifier.height((12 * u).dp))

                        // 2. Middle Scrollable Content
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy((14 * u).dp)
                            ) {
                                PrayerAutoSilentConfigSection(
                                    isAr = isAr,
                                    u = u,
                                    minTouchTargetDp = minTouchTargetDp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height((14 * u).dp))

                        // 3. Fixed Bottom Action Button (تم / Done)
                        Button(
                            onClick = onConfirmed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((52 * u).dp),
                            shape = RoundedCornerShape((14 * u).dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (isAr) "تم" else "Done",
                                fontWeight = FontWeight.Bold,
                                fontSize = (16 * u).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerAutoSilentConfigSection(
    isAr: Boolean = true,
    u: Float = 1f,
    minTouchTargetDp: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val silentPrefs = remember { PrayerAutoSilentManager.getPrefs(context) }

    var autoSilentEnabled by remember {
        mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_ENABLED, false))
    }
    var silentModeType by remember {
        mutableStateOf(silentPrefs.getString(PrayerAutoSilentManager.KEY_MODE_TYPE, "silent") ?: "silent")
    }
    var transitionAlertEnabled by remember {
        mutableStateOf(silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_TRANSITION_ALERT, true))
    }

    // Dynamic DND permission tracking: automatically re-checked on every resume
    var hasPolicyAccess by remember {
        mutableStateOf(PrayerAutoSilentManager.hasNotificationPolicyAccess(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPolicyAccess = PrayerAutoSilentManager.hasNotificationPolicyAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // State maps for per-prayer values
    val enabledMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            PRAYER_SILENT_CONFIGS.forEach { cfg ->
                put(cfg.key, silentPrefs.getBoolean(cfg.enabledPrefKey, true))
            }
        }
    }
    val delayMap = remember {
        mutableStateMapOf<String, Int>().apply {
            val globalDelay = silentPrefs.getInt(PrayerAutoSilentManager.KEY_DELAY_MINUTES, 0)
            PRAYER_SILENT_CONFIGS.forEach { cfg ->
                put(cfg.key, silentPrefs.getInt(cfg.delayPrefKey, if (globalDelay > 0) globalDelay else cfg.defaultDelay))
            }
        }
    }
    val durationMap = remember {
        mutableStateMapOf<String, Int>().apply {
            val globalDuration = silentPrefs.getInt(PrayerAutoSilentManager.KEY_DURATION_MINUTES, 20)
            PRAYER_SILENT_CONFIGS.forEach { cfg ->
                put(cfg.key, silentPrefs.getInt(cfg.durationPrefKey, if (globalDuration != 20) globalDuration else cfg.defaultDuration))
            }
        }
    }

    // Expanded card tracker (expanded key or null)
    var expandedPrayerKey by remember { mutableStateOf<String?>("fajr") }

    // Dialog state for custom minute input
    var customDialogState by remember { mutableStateOf<CustomDialogState?>(null) }

    Card(
        shape = RoundedCornerShape((16 * u).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke((1 * u).dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding((14 * u).dp),
            verticalArrangement = Arrangement.spacedBy((14 * u).dp)
        ) {
            // Master Switch Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = (56 * u).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((10 * u).dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size((38 * u).dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size((20 * u).dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isAr) "الوضع الصامت التلقائي أثناء الصلاة" else "Auto-Silent Mode During Prayer",
                            fontSize = (15 * u).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isAr) "كتم الهاتف واستعادة الصوت تلقائياً بعد الصلاة" else "Mute phone and auto-restore after prayer",
                            fontSize = (12 * u).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = autoSilentEnabled,
                    onCheckedChange = { isChecked ->
                        autoSilentEnabled = isChecked
                        silentPrefs.edit().putBoolean(PrayerAutoSilentManager.KEY_ENABLED, isChecked).apply()
                        if (isChecked) {
                            hasPolicyAccess = PrayerAutoSilentManager.hasNotificationPolicyAccess(context)
                        } else if (silentPrefs.getBoolean(PrayerAutoSilentManager.KEY_IS_ACTIVE, false)) {
                            PrayerAutoSilentManager.restoreNormalMode(context, showToast = true)
                        }
                    },
                    modifier = Modifier.scale(0.9f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            AnimatedVisibility(
                visible = autoSilentEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy((14 * u).dp)) {

                    // 1. Dynamic DND Permission Notice (Automatically hides when granted)
                    if (!hasPolicyAccess) {
                        Surface(
                            shape = RoundedCornerShape((12 * u).dp),
                            color = Color(0xFFD97706).copy(alpha = 0.12f),
                            border = BorderStroke((1 * u).dp, Color(0xFFD97706).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding((12 * u).dp),
                                verticalArrangement = Arrangement.spacedBy((6 * u).dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((8 * u).dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size((18 * u).dp)
                                    )
                                    Text(
                                        text = if (isAr) "يلزم إذن الوصول لعدم الإزعاج لتفعيل الصامت" else "Requires Do Not Disturb permission",
                                        fontSize = (13.5 * u).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = if (isAr)
                                        "يطلب أندرويد إذناً خاصاً للسماح للتطبيق بكتم رنين الهاتف. انقر أدناه لمنحه وسيتعرف التطبيق عليه تلقائياً فور عودتك."
                                    else
                                        "Android requires permission to change ringer mode. Tap below to grant and return.",
                                    fontSize = (12 * u).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        PrayerAutoSilentManager.openNotificationPolicySettings(context)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((44 * u).dp),
                                    shape = RoundedCornerShape((10 * u).dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Text(
                                        text = if (isAr) "منح إذن عدم الإزعاج في إعدادات الهاتف (مرة واحدة)" else "Grant Permission in Settings",
                                        fontSize = (12.5 * u).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        // Reassuring Green Badge when granted
                        Surface(
                            shape = RoundedCornerShape((10 * u).dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f),
                            border = BorderStroke((1 * u).dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = (12 * u).dp, vertical = (8 * u).dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((8 * u).dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size((16 * u).dp)
                                )
                                Text(
                                    text = if (isAr) "إذن عدم الإزعاج مفعّل ومصرّح له بنجاح ✓" else "Do Not Disturb permission granted ✓",
                                    fontSize = (12.5 * u).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // 2. Silent Type (Silent vs Vibrate)
                    Column(verticalArrangement = Arrangement.spacedBy((6 * u).dp)) {
                        Text(
                            text = if (isAr) "نوع الكتم:" else "Silent Type:",
                            fontSize = (15 * u).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((48 * u).dp)
                                .clip(RoundedCornerShape((10 * u).dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding((3 * u).dp),
                            horizontalArrangement = Arrangement.spacedBy((4 * u).dp)
                        ) {
                            val isSilent = silentModeType == "silent"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape((8 * u).dp))
                                    .background(if (isSilent) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        silentModeType = "silent"
                                        silentPrefs.edit().putString(PrayerAutoSilentManager.KEY_MODE_TYPE, "silent").apply()
                                    }
                                    .padding(vertical = (7 * u).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) "🔕 صامت تام" else "🔕 Total Silent",
                                    color = if (isSilent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (13.5 * u).sp,
                                    maxLines = 1
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape((8 * u).dp))
                                    .background(if (!isSilent) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        silentModeType = "vibrate"
                                        silentPrefs.edit().putString(PrayerAutoSilentManager.KEY_MODE_TYPE, "vibrate").apply()
                                    }
                                    .padding(vertical = (7 * u).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) "📳 اهتزاز فقط" else "📳 Vibrate Only",
                                    color = if (!isSilent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (13.5 * u).sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 3. Gentle Transition Alert Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = (48 * u).dp)
                            .clickable {
                                val updated = !transitionAlertEnabled
                                transitionAlertEnabled = updated
                                silentPrefs.edit().putBoolean(PrayerAutoSilentManager.KEY_TRANSITION_ALERT, updated).apply()
                            }
                            .padding(vertical = (4 * u).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = (8 * u).dp)) {
                            Text(
                                text = if (isAr) "تنبيه خفيف عند التحويل للصامت والخروج منه" else "Gentle transition alert",
                                fontSize = (14 * u).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAr) "نغمة هادئة واهتزاز خفيف لراحتك أثناء الصلاة" else "Soft chime & haptic feedback",
                                fontSize = (11.5 * u).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = transitionAlertEnabled,
                            onCheckedChange = { checked ->
                                transitionAlertEnabled = checked
                                silentPrefs.edit().putBoolean(PrayerAutoSilentManager.KEY_TRANSITION_ALERT, checked).apply()
                            },
                            modifier = Modifier.scale(0.85f)
                        )
                    }

                    // 4. Per-Prayer Unlimited Controls Section
                    Column(verticalArrangement = Arrangement.spacedBy((8 * u).dp)) {
                        Text(
                            text = if (isAr) "تخصيص أوقات ومدة الصامت لكل صلاة:" else "Per-Prayer Silent Timing & Duration:",
                            fontSize = (15 * u).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // List of prayers
                        PRAYER_SILENT_CONFIGS.forEach { cfg ->
                            val isEnabled = enabledMap[cfg.key] ?: true
                            val delayVal = delayMap[cfg.key] ?: cfg.defaultDelay
                            val durationVal = durationMap[cfg.key] ?: cfg.defaultDuration
                            val isExpanded = expandedPrayerKey == cfg.key

                            PrayerSilentCard(
                                cfg = cfg,
                                isAr = isAr,
                                u = u,
                                minTouchTargetDp = minTouchTargetDp,
                                isEnabled = isEnabled,
                                delayVal = delayVal,
                                durationVal = durationVal,
                                isExpanded = isExpanded,
                                onToggleEnabled = { checked ->
                                    enabledMap[cfg.key] = checked
                                    silentPrefs.edit().putBoolean(cfg.enabledPrefKey, checked).apply()
                                },
                                onCardClick = {
                                    expandedPrayerKey = if (isExpanded) null else cfg.key
                                },
                                onUpdateDelay = { newDelay ->
                                    val clamped = newDelay.coerceAtLeast(0)
                                    delayMap[cfg.key] = clamped
                                    silentPrefs.edit().putInt(cfg.delayPrefKey, clamped).apply()
                                },
                                onUpdateDuration = { newDur ->
                                    val clamped = newDur.coerceAtLeast(1)
                                    durationMap[cfg.key] = clamped
                                    silentPrefs.edit().putInt(cfg.durationPrefKey, clamped).apply()
                                },
                                onOpenCustomDelayDialog = {
                                    customDialogState = CustomDialogState(cfg, CustomValueType.DELAY, delayVal)
                                },
                                onOpenCustomDurationDialog = {
                                    customDialogState = CustomDialogState(cfg, CustomValueType.DURATION, durationVal)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Custom Minutes Input Dialog (Unlimited input for start delay or prayer duration)
    customDialogState?.let { state ->
        var inputText by remember { mutableStateOf(state.currentValue.toString()) }
        var isError by remember { mutableStateOf(false) }

        val title = if (state.type == CustomValueType.DELAY) {
            if (isAr) "وقت بدء الصامت بعد الأذان (${state.prayerConfig.nameAr})" else "Start Delay (${state.prayerConfig.nameEn})"
        } else {
            if (isAr) "مدة الوضع الصامت (${state.prayerConfig.nameAr})" else "Silent Duration (${state.prayerConfig.nameEn})"
        }

        val hint = if (state.type == CustomValueType.DELAY) {
            if (isAr) "أدخل عدد الدقائق بعد الأذان (0 = فوراً)" else "Enter minutes after Adhan (0 = immediate)"
        } else {
            if (isAr) "أدخل مدة بقاء الوضع الصامت بالدقائق" else "Enter duration in minutes"
        }

        AlertDialog(
            onDismissRequest = { customDialogState = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = (360 * u).dp),
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = (17 * u).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy((8 * u).dp)) {
                    Text(
                        text = hint,
                        fontSize = (13 * u).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { input ->
                            inputText = input.filter { it.isDigit() }
                            isError = false
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = isError,
                        label = { Text(if (isAr) "المدة بالدقائق" else "Minutes", fontSize = (13 * u).sp) },
                        suffix = { Text(if (isAr) "دقيقة" else "min", fontSize = (13 * u).sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                            text = if (isAr) "يرجى إدخال رقم صحيح" else "Please enter a valid number",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = (11.5 * u).sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputText.toIntOrNull()
                        if (num != null && num >= 0) {
                            if (state.type == CustomValueType.DELAY) {
                                delayMap[state.prayerConfig.key] = num
                                silentPrefs.edit().putInt(state.prayerConfig.delayPrefKey, num).apply()
                            } else {
                                val validDuration = num.coerceAtLeast(1)
                                durationMap[state.prayerConfig.key] = validDuration
                                silentPrefs.edit().putInt(state.prayerConfig.durationPrefKey, validDuration).apply()
                            }
                            customDialogState = null
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.height((42 * u).dp)
                ) {
                    Text(if (isAr) "حفظ" else "Save", fontSize = (13.5 * u).sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { customDialogState = null },
                    modifier = Modifier.height((42 * u).dp)
                ) {
                    Text(if (isAr) "إلغاء" else "Cancel", fontSize = (13.5 * u).sp)
                }
            }
        )
    }
}

@Composable
private fun PrayerSilentCard(
    cfg: PrayerSilentItemConfig,
    isAr: Boolean,
    u: Float,
    minTouchTargetDp: Dp,
    isEnabled: Boolean,
    delayVal: Int,
    durationVal: Int,
    isExpanded: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    onUpdateDelay: (Int) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onOpenCustomDelayDialog: () -> Unit,
    onOpenCustomDurationDialog: () -> Unit
) {
    val prayerName = if (isAr) cfg.nameAr else cfg.nameEn

    var delayInputText by remember(delayVal) { mutableStateOf(delayVal.toString()) }
    var durationInputText by remember(durationVal) { mutableStateOf(durationVal.toString()) }

    Surface(
        shape = RoundedCornerShape((12 * u).dp),
        color = if (isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            (1 * u).dp,
            if (isEnabled) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding((10 * u).dp)) {
            // Header Row: Name, Quick Summary & Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = (48 * u).dp)
                    .clickable { onCardClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * u).dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size((20 * u).dp)
                    )
                    Column {
                        Text(
                            text = prayerName,
                            fontSize = (15 * u).sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isEnabled) {
                            val delaySummary = if (delayVal == 0) (if (isAr) "فور الأذان" else "At Adhan") else "$delayVal ${if (isAr) "د بعده" else "m after"}"
                            Text(
                                text = "${if (isAr) "البدء:" else "Start:"} $delaySummary | ${if (isAr) "المدة:" else "Dur:"} $durationVal ${if (isAr) "دقيقة" else "min"}",
                                fontSize = (11.5 * u).sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            // Expanded detail controls
            AnimatedVisibility(
                visible = isEnabled && isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = (10 * u).dp),
                    verticalArrangement = Arrangement.spacedBy((12 * u).dp)
                ) {
                    // A. Delay Controls (وقت بدء الصامت - كتابة مباشرة وتأكيد)
                    Column(verticalArrangement = Arrangement.spacedBy((6 * u).dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((4 * u).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size((15 * u).dp)
                            )
                            Text(
                                text = if (isAr) "بدء الوضع الصامت بعد الأذان (بالدقائق):" else "Start Silent After Adhan (Minutes):",
                                fontSize = (12.5 * u).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Direct Text Field + Confirm Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((8 * u).dp)
                        ) {
                            OutlinedTextField(
                                value = delayInputText,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    delayInputText = filtered
                                    val parsed = filtered.toIntOrNull()
                                    if (parsed != null && parsed >= 0) {
                                        onUpdateDelay(parsed)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                suffix = {
                                    Text(
                                        text = if (isAr) "دقيقة (0=فوراً)" else "min (0=instant)",
                                        fontSize = (11 * u).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape((10 * u).dp),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    val parsed = delayInputText.toIntOrNull()
                                    if (parsed != null && parsed >= 0) {
                                        onUpdateDelay(parsed)
                                    } else {
                                        delayInputText = delayVal.toString()
                                    }
                                },
                                shape = RoundedCornerShape((10 * u).dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.height((50 * u).dp)
                            ) {
                                Text(
                                    text = if (isAr) "تأكيد" else "Confirm",
                                    fontSize = (12.5 * u).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = (1 * u).dp
                    )

                    // B. Duration Controls (مدة بقاء الوضع الصامت - كتابة مباشرة وتأكيد)
                    Column(verticalArrangement = Arrangement.spacedBy((6 * u).dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((4 * u).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size((15 * u).dp)
                            )
                            Text(
                                text = if (isAr) "مدة بقاء الوضع الصامت (بالدقائق):" else "Silent Duration (Minutes):",
                                fontSize = (12.5 * u).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Direct Text Field + Confirm Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((8 * u).dp)
                        ) {
                            OutlinedTextField(
                                value = durationInputText,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    durationInputText = filtered
                                    val parsed = filtered.toIntOrNull()
                                    if (parsed != null && parsed >= 1) {
                                        onUpdateDuration(parsed)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                suffix = {
                                    Text(
                                        text = if (isAr) "دقيقة كتم" else "min silent",
                                        fontSize = (11 * u).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape((10 * u).dp),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    val parsed = durationInputText.toIntOrNull()
                                    if (parsed != null && parsed >= 1) {
                                        onUpdateDuration(parsed)
                                    } else {
                                        durationInputText = durationVal.toString()
                                    }
                                },
                                shape = RoundedCornerShape((10 * u).dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.height((50 * u).dp)
                            ) {
                                Text(
                                    text = if (isAr) "تأكيد" else "Confirm",
                                    fontSize = (12.5 * u).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
