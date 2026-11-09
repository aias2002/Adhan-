package com.example.ui.adhan

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.service.AdhanPlaybackService
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.AppLocalization
import com.example.util.AdhanScheduler
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdhanAlarmActivity : ComponentActivity() {

    private val adhanStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AdhanPlaybackService.ACTION_ADHAN_STOPPED) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen, unlock, and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prayerName = intent?.getStringExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME) ?: "الصلاة"
        val isSunrise = intent?.getBooleanExtra(AdhanPlaybackService.EXTRA_IS_SUNRISE, false) ?: false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                adhanStopReceiver,
                IntentFilter(AdhanPlaybackService.ACTION_ADHAN_STOPPED),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                adhanStopReceiver,
                IntentFilter(AdhanPlaybackService.ACTION_ADHAN_STOPPED)
            )
        }

        enableEdgeToEdge()
        setContent {
            val prefs = remember { AdhanScheduler.getPrefs(this@AdhanAlarmActivity) }
            val themePalette = remember { prefs.getString("theme_palette", "EMERALD") ?: "EMERALD" }
            val numeralSystem = remember { prefs.getString("numeral_system", "arabic") ?: "arabic" }
            val appLanguage = remember { prefs.getString("app_language", "ar") ?: "ar" }

            MyApplicationTheme(themePalette = themePalette, darkTheme = true) {
                AdhanAlarmScreen(
                    prayerName = prayerName,
                    isSunrise = isSunrise,
                    numeralSystem = numeralSystem,
                    appLanguage = appLanguage,
                    onStopAdhan = {
                        val stopIntent = Intent(this@AdhanAlarmActivity, AdhanPlaybackService::class.java).apply {
                            action = AdhanPlaybackService.ACTION_STOP
                        }
                        startService(stopIntent)
                        finish()
                    },
                    onOpenApp = {
                        val mainIntent = Intent(this@AdhanAlarmActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(adhanStopReceiver)
        } catch (_: Exception) {}
        super.onDestroy()
    }
}

@Composable
fun AdhanAlarmScreen(
    prayerName: String,
    isSunrise: Boolean,
    numeralSystem: String,
    appLanguage: String,
    onStopAdhan: () -> Unit,
    onOpenApp: () -> Unit
) {
    var currentTimeStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("hh:mm", Locale.US)
        while (true) {
            currentTimeStr = sdf.format(Date())
            delay(1000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "adhan_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF044E3B), // Rich Luminous Emerald
            Color(0xFF0D9488), // Vibrant Teal
            Color(0xFF065F46), // Deep Emerald
            Color(0xFF0F172A)  // Deep Midnight Slate
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Pill Tag
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    color = Color(0xFFFEF3C7).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFDE047))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSunrise) "شروق الشمس" else "نداء الصلاة • الأذان",
                            color = Color(0xFFFEF08A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = CairoFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Current Time Display
                if (currentTimeStr.isNotEmpty()) {
                    val formattedTime = AppLocalization.formatDigits(currentTimeStr, numeralSystem)
                    Text(
                        text = formattedTime,
                        fontSize = 62.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = CairoFontFamily,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Center: Glowing Animated Icon & Prayer Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    // Outer pulsing ring
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE047).copy(alpha = ringAlpha * 0.35f))
                            .border(
                                width = 3.dp,
                                color = Color(0xFFFDE047).copy(alpha = ringAlpha),
                                shape = CircleShape
                            )
                    )

                    // Inner circle
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF047857)
                                    )
                                )
                            )
                            .border(3.dp, Color(0xFFFDE047), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSunrise) Icons.Default.Schedule else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                val titleText = if (isSunrise) {
                    "حان وقت الشروق"
                } else {
                    "حان الآن موعد أذان $prayerName"
                }

                Text(
                    text = titleText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = CairoFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                val subtitleText = if (isSunrise) {
                    "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ"
                } else {
                    "حي على الصلاة • حي على الفلاح"
                }

                Text(
                    text = subtitleText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE047),
                    fontFamily = CairoFontFamily,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom Actions & Guidance
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Stop Adhan Button (Prominent Vivid Red with high contrast)
                Button(
                    onClick = onStopAdhan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSunrise) "إيقاف التنبيه" else "إيقاف صوت الأذان",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = CairoFontFamily,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Open App Button (Crisp White Border on translucent background)
                OutlinedButton(
                    onClick = onOpenApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(2.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "فتح تطبيق Adhan+",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Flip to mute hint
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "💡 يمكنك أيضاً قلب الهاتف على وجهه لكتم الصوت فوراً",
                            color = Color(0xFFF1F5F9),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = CairoFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
