package com.example.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.hardware.SensorManager
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PRESET_BACKGROUND_THEMES
import com.example.ui.theme.CairoFontFamily
import com.example.util.CompassSensorManager
import com.example.util.QiblaCalculator
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    userLat: Double,
    userLng: Double,
    cityName: String,
    onLocationUpdate: ((lat: Double, lng: Double, cityName: String) -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = onOpenDrawer,
    appLanguage: String = "ar",
    isDarkMode: Boolean = false,
    backgroundThemeId: String = "emerald",
    modifier: Modifier = Modifier
) {
    val isAr = appLanguage != "en"
    val numeralSystem = com.example.ui.util.AppLocalization.LocalNumeralSystem.current
    val theme = PRESET_BACKGROUND_THEMES.find { it.id == backgroundThemeId } ?: PRESET_BACKGROUND_THEMES.first()
    val backgroundColor = if (isDarkMode) Color(theme.darkBgColor) else Color(theme.lightBgColor)
    val cardColor = if (isDarkMode) Color(theme.darkCardColor) else Color(theme.lightCardColor)
    val cardBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color(0x1F0F4D3A)
    val primaryTextColor = if (isDarkMode) Color(0xFFF1F5F3) else Color(0xFF12261D)
    val secondaryTextColor = if (isDarkMode) Color(0xFFA2B4AB) else Color(0xFF55665D)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // GPS Permission launcher
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            com.example.ui.components.getGpsLocation(context) { loc ->
                if (loc != null && onLocationUpdate != null) {
                    onLocationUpdate(loc.latitude, loc.longitude, "موقعي الحالي (GPS)")
                }
            }
        }
    }

    // Unchanged mathematical calculations
    val qiblaBearing = remember(userLat, userLng) {
        QiblaCalculator.calculateQiblaBearing(userLat, userLng)
    }
    val declination = remember(userLat, userLng) {
        QiblaCalculator.getDeclination(userLat, userLng)
    }
    val distanceKm = remember(userLat, userLng) {
        QiblaCalculator.calculateDistanceToKaaba(userLat, userLng)
    }

    var rawAzimuth by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var smoothedAzimuth by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val sensorManager = CompassSensorManager(context)
        sensorManager.compassDataFlow.collectLatest { data ->
            rawAzimuth = data.azimuth
            sensorAccuracy = data.accuracy

            // Much faster and cleaner response. CompassSensorManager already did great EMA filtering.
            val diff = (data.azimuth - smoothedAzimuth + 540f) % 360f - 180f
            smoothedAzimuth = (smoothedAzimuth + diff * 0.60f + 360f) % 360f
        }
    }

    val trueAzimuth = (smoothedAzimuth + declination + 360f) % 360f
    var accumulatedAzimuth by remember { mutableFloatStateOf(trueAzimuth) }

    LaunchedEffect(trueAzimuth) {
        val delta = (trueAzimuth - (accumulatedAzimuth % 360f) + 540f) % 360f - 180f
        accumulatedAzimuth += delta
    }

    // High performance spring physics animation to make rotation liquid-smooth and instantaneous to phone movement
    val animatedAzimuth by animateFloatAsState(
        targetValue = accumulatedAzimuth,
        animationSpec = spring(
            dampingRatio = 0.85f, // beautiful, steady, no oscillations
            stiffness = Spring.StiffnessHigh // fast following
        ),
        label = "compass_ease_out_rotation"
    )

    val currentHeading = (animatedAzimuth % 360f + 360f) % 360f
    val relativeDiff = (qiblaBearing - currentHeading + 540f) % 360f - 180f
    val isAligned = abs(relativeDiff) <= 3.0f

    // Get physical device vibration service
    val vibrator = remember(context) {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Trigger subtle pulsing physical vibration while aligned to make finding Qibla effortless and satisfying
    LaunchedEffect(isAligned) {
        if (isAligned && vibrator != null) {
            val audioAttributes = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM) // Bypasses touch haptic mutes in system settings!
                    .build()
            } else null

            while (true) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && audioAttributes != null) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE),
                            audioAttributes
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(180)
                    }
                } catch (_: Exception) {}
                kotlinx.coroutines.delay(700)
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp)
            ) {
                val screenWidth = maxWidth
                val compassDiameter = minOf(320.dp, screenWidth - 48.dp).coerceAtLeast(220.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Header (الرأس): زر القائمة وزر الموقع على الطرف البادئ (يمين في العربي، يسار في الإنجليزي)، والعنوان على الطرف المقابل
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular buttons 48px at Start (Right in RTL, Left in LTR)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Settings Button: 48px, white background, subtle border
                            val openAction = onOpenSettings ?: onOpenDrawer
                            if (openAction != null) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clickable(onClick = openAction)
                                        .testTag("btn_open_settings_qibla"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_settings_tasbih),
                                        contentDescription = if (isAr) "الإعدادات" else "Settings",
                                        tint = if (isDarkMode) Color(0xFFFFD60A) else primaryTextColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Location Refresh Button: 48px
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkMode) Color(0xFF1E3A2F) else Color(0xFFD6F0E2))
                                    .clickable {
                                        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                        if (hasFine || hasCoarse) {
                                            com.example.ui.components.getGpsLocation(context) { loc ->
                                                if (loc != null && onLocationUpdate != null) {
                                                    onLocationUpdate(loc.latitude, loc.longitude, "موقعي الحالي (GPS)")
                                                }
                                            }
                                        } else {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                    .testTag("btn_refresh_qibla_gps"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "تحديد الموقع",
                                    tint = if (isDarkMode) Color(0xFF68D391) else Color(0xFF0F4D3A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Title & Location subtitle at End
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isAr) "بوصلة القبلة" else "Qibla Compass",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = CairoFontFamily,
                                color = primaryTextColor,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )
                            val formattedDist = com.example.ui.util.AppLocalization.formatDigits(String.format(Locale.US, "%.0f", distanceKm), numeralSystem)
                            Text(
                                text = "${cityName} · ${formattedDist} ${if (isAr) "كم إلى مكة" else "km to Makkah"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = CairoFontFamily,
                                color = secondaryTextColor,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // 2. Status Card (بطاقة الحالة): تحت الرأس بزوايا 24px
                    val defaultStatusCardBg = if (isDarkMode) cardColor else Color.White
                    val statusCardBg by animateColorAsState(
                        targetValue = if (isAligned) Color(0xFF0F4D3A) else defaultStatusCardBg,
                        animationSpec = tween(300),
                        label = "status_card_bg"
                    )
                    val statusCardBorder = if (isAligned) Color.Transparent else cardBorderColor

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = statusCardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, statusCardBorder, RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Status Icon
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isAligned) Color.White.copy(alpha = 0.15f)
                                        else if (isDarkMode) Color(0xFF1E3A2F)
                                        else Color(0xFFD6F0E2)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAligned) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFE3B94B),
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else if (relativeDiff > 3f) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = if (isAr) "أدر يميناً" else "Turn right",
                                        tint = if (isDarkMode) Color(0xFF68D391) else Color(0xFF0F4D3A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = if (isAr) "أدر يساراً" else "Turn left",
                                        tint = if (isDarkMode) Color(0xFF68D391) else Color(0xFF0F4D3A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Status Text lines
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val titleText = when {
                                    isAligned -> if (isAr) "متجه نحو القبلة" else "Facing Qibla"
                                    relativeDiff > 3f -> {
                                        val diffDigits = com.example.ui.util.AppLocalization.formatDigits(relativeDiff.toInt().toString(), numeralSystem)
                                        if (isAr) "أدر يميناً ${diffDigits}°" else "Turn right ${diffDigits}°"
                                    }
                                    else -> {
                                        val diffDigits = com.example.ui.util.AppLocalization.formatDigits(abs(relativeDiff).toInt().toString(), numeralSystem)
                                        if (isAr) "أدر يساراً ${diffDigits}°" else "Turn left ${diffDigits}°"
                                    }
                                }

                                val subtitleText = when {
                                    isAligned -> if (isAr) "الكعبة أمامك مباشرة" else "Kaaba is directly in front of you"
                                    else -> if (isAr) "لتطابق السهم الأخضر" else "To align with the green arrow"
                                }

                                Text(
                                    text = titleText,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily,
                                    color = if (isAligned) Color.White else primaryTextColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = subtitleText,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = CairoFontFamily,
                                    color = if (isAligned) Color(0xFFD6F0E2) else secondaryTextColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // 3. Compass Dial (البوصلة): قرص دائري داخل إطار ذهبي متدرج #F1D77A -> #C79A2B بسمك 6px
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(compassDiameter)
                            .testTag("qibla_compass_dial")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val frameStroke = 6.dp.toPx()
                            val outerRadius = size.minDimension / 2f - frameStroke / 2f
                            val innerRadius = outerRadius - frameStroke / 2f

                            // 1. Dial Background
                            val dialBgColor = if (isDarkMode) Color(0xFF16201C) else Color(0xFFFCFCFB)
                            drawCircle(
                                color = dialBgColor,
                                radius = innerRadius,
                                center = center
                            )

                            // 2. Gradient Golden Outer Frame #F1D77A -> #C79A2B (thickness 6px)
                            val goldenBrush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFF1D77A),
                                    Color(0xFFC79A2B),
                                    Color(0xFFF1D77A),
                                    Color(0xFFC79A2B),
                                    Color(0xFFF1D77A)
                                ),
                                center = center
                            )
                            drawCircle(
                                brush = goldenBrush,
                                radius = outerRadius,
                                center = center,
                                style = Stroke(width = frameStroke)
                            )

                            // 3. Rotating Dial Layer (-animatedAzimuth)
                            rotate(-animatedAzimuth, pivot = center) {
                                // Ticks: every 5° faint, every 30° dark green #0F4D3A
                                for (deg in 0 until 360 step 5) {
                                    val isMajor = deg % 30 == 0
                                    val angleRad = Math.toRadians(deg.toDouble())
                                    val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                                    val tickStroke = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                                    val tickColor = if (isMajor) {
                                        if (isDarkMode) Color(0xFF68D391) else Color(0xFF0F4D3A)
                                    } else {
                                        if (isDarkMode) Color(0x3368D391) else Color(0x330F4D3A)
                                    }

                                    val startX = center.x + (innerRadius - 4.dp.toPx()) * sin(angleRad).toFloat()
                                    val startY = center.y - (innerRadius - 4.dp.toPx()) * cos(angleRad).toFloat()
                                    val endX = center.x + (innerRadius - 4.dp.toPx() - tickLen) * sin(angleRad).toFloat()
                                    val endY = center.y - (innerRadius - 4.dp.toPx() - tickLen) * cos(angleRad).toFloat()

                                    drawLine(
                                        color = tickColor,
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = tickStroke
                                    )
                                }

                                // Cardinal Directions: ش (أحمر #C23B3B)، ق، ج، غ (#0F4D3A)
                                val cardinalOtherColor = if (isDarkMode) Color(0xFF81E6D9) else Color(0xFF0F4D3A)
                                val cardinalLabels = if (isAr) listOf(
                                    Triple("ش", 0f, Color(0xFFC23B3B)),
                                    Triple("ق", 90f, cardinalOtherColor),
                                    Triple("ج", 180f, cardinalOtherColor),
                                    Triple("غ", 270f, cardinalOtherColor)
                                ) else listOf(
                                    Triple("N", 0f, Color(0xFFC23B3B)),
                                    Triple("E", 90f, cardinalOtherColor),
                                    Triple("S", 180f, cardinalOtherColor),
                                    Triple("W", 270f, cardinalOtherColor)
                                )
                                cardinalLabels.forEach { (label, angle, color) ->
                                    val angleRad = Math.toRadians(angle.toDouble())
                                    val labelRadius = innerRadius - 24.dp.toPx()
                                    val lx = center.x + labelRadius * sin(angleRad).toFloat()
                                    val ly = center.y - labelRadius * cos(angleRad).toFloat()

                                    val style = TextStyle(
                                        color = color,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily
                                    )
                                    val measured = textMeasurer.measure(label, style)
                                    drawText(
                                        textLayoutResult = measured,
                                        topLeft = Offset(
                                            lx - measured.size.width / 2f,
                                            ly - measured.size.height / 2f
                                        )
                                    )
                                }

                                // Golden dotted line from center to Kaaba at qiblaBearing
                                val qiblaAngleRad = Math.toRadians(qiblaBearing.toDouble())
                                val kaabaBadgeRadius = innerRadius - 16.dp.toPx()
                                val kaabaCenterX = center.x + kaabaBadgeRadius * sin(qiblaAngleRad).toFloat()
                                val kaabaCenterY = center.y - kaabaBadgeRadius * cos(qiblaAngleRad).toFloat()

                                drawLine(
                                    color = Color(0xFFC79A2B),
                                    start = center,
                                    end = Offset(kaabaCenterX, kaabaCenterY),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )

                                // Kaaba Badge at rim of disc at qiblaBearing
                                val badgeOuterRadius = 17.dp.toPx()
                                drawCircle(
                                    color = Color.White,
                                    radius = badgeOuterRadius,
                                    center = Offset(kaabaCenterX, kaabaCenterY)
                                )
                                drawCircle(
                                    color = Color(0xFFC79A2B),
                                    radius = badgeOuterRadius,
                                    center = Offset(kaabaCenterX, kaabaCenterY),
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Counter-rotate Kaaba icon by (animatedAzimuth - qiblaBearing) so it remains upright
                                rotate(animatedAzimuth - qiblaBearing, pivot = Offset(kaabaCenterX, kaabaCenterY)) {
                                    val cubeW = 18.dp.toPx()
                                    val cubeH = 18.dp.toPx()
                                    val cubeTopLeft = Offset(kaabaCenterX - cubeW / 2f, kaabaCenterY - cubeH / 2f)

                                    // Black cube
                                    drawRoundRect(
                                        color = Color(0xFF1E1E1E),
                                        topLeft = cubeTopLeft,
                                        size = Size(cubeW, cubeH),
                                        cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
                                    )

                                    // Golden Kiswah belt
                                    drawRect(
                                        color = Color(0xFFE3B94B),
                                        topLeft = Offset(cubeTopLeft.x, cubeTopLeft.y + 3.5.dp.toPx()),
                                        size = Size(cubeW, 2.5.dp.toPx())
                                    )

                                    // Golden door
                                    drawRect(
                                        color = Color(0xFFE3B94B),
                                        topLeft = Offset(cubeTopLeft.x + 2.dp.toPx(), cubeTopLeft.y + 8.dp.toPx()),
                                        size = Size(3.5.dp.toPx(), 7.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Center Hub: circular disc with golden border and mosque icon on green background
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isDarkMode) cardColor else Color.White)
                                .border(2.dp, Color(0xFFC79A2B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F4D3A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Fixed green arrow at top of dial representing front of phone
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 1.dp)
                        ) {
                            Canvas(modifier = Modifier.size(22.dp, 16.dp)) {
                                val arrowPath = Path().apply {
                                    moveTo(size.width / 2f, size.height) // tip pointing into dial
                                    lineTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    close()
                                }
                                val frontArrowColor = if (isDarkMode) Color(0xFF68D391) else Color(0xFF0F4D3A)
                                drawPath(path = arrowPath, color = frontArrowColor)
                            }
                        }
                    }

                    // 4. Three Adjacent Cards: «اتجاه القبلة»، «اتجاه هاتفك»، «الفرق»
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. اتجاه القبلة
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(18.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isAr) "اتجاه القبلة" else "Qibla Direction",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = CairoFontFamily,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    val bearingStr = com.example.ui.util.AppLocalization.formatDigits(String.format(Locale.US, "%.1f", qiblaBearing), numeralSystem)
                                    Text(
                                        text = "${bearingStr}°",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = primaryTextColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // 2. اتجاه هاتفك
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(18.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isAr) "اتجاه هاتفك" else "Phone Heading",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = CairoFontFamily,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    val headingStr = com.example.ui.util.AppLocalization.formatDigits(String.format(Locale.US, "%.0f", currentHeading), numeralSystem)
                                    Text(
                                        text = "${headingStr}°",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = primaryTextColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // 3. الفرق: أخضر عند التطابق، وإلا أحمر
                        val diffColor = if (isAligned) {
                            if (isDarkMode) Color(0xFF48BB78) else Color(0xFF0F7A4F)
                        } else {
                            if (isDarkMode) Color(0xFFFC8181) else Color(0xFFB4472F)
                        }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, cardBorderColor, RoundedCornerShape(18.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isAr) "الفرق" else "Difference",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = CairoFontFamily,
                                    color = secondaryTextColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    val diffStr = com.example.ui.util.AppLocalization.formatDigits(abs(relativeDiff).toInt().toString(), numeralSystem)
                                    Text(
                                        text = "${diffStr}°",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = diffColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // 5. Calibration / Warning Card
                    val guideCardBg = if (isDarkMode) Color(0xFF282318) else Color(0xFFFFF4D6)
                    val guideCardBorder = if (isDarkMode) Color(0xFF4D4024) else Color(0xFFF0DFAC)
                    val guideTextColor = if (isDarkMode) Color(0xFFFFD54F) else Color(0xFF5E4710)
                    val guideIconTint = if (isDarkMode) Color(0xFFFFCA28) else Color(0xFFB8860B)

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = guideCardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, guideCardBorder, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompassCalibration,
                                    contentDescription = null,
                                    tint = guideIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isAr) "إرشادات دقة البوصلة" else "Compass Accuracy Guide",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFontFamily,
                                    color = guideTextColor
                                )
                            }
                            Text(
                                text = if (isAr)
                                    "أبقِ الهاتف أفقيًا على سطح مستوٍ، وابتعد عن المعادن والمغناطيس، ولوّح بالهاتف على شكل 8 لمعايرة البوصلة."
                                else
                                    "Keep phone flat on a level surface, stay away from metals/magnets, and wave phone in figure-8 pattern to calibrate.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = CairoFontFamily,
                                color = guideTextColor,
                                lineHeight = 18.sp
                            )

                            // Status badge if sensor accuracy is low
                            if (sensorAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
                                sensorAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkMode) Color(0xFF421D1D) else Color(0xFFE57373).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isAr) "⚠️ دقة المستشعر منخفضة - يُرجى المعايرة" else "⚠️ Low sensor accuracy - Please calibrate",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
