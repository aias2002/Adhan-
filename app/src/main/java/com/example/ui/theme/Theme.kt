package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

// Unified Design System Colors
val PrimaryDarkGreen = Color(0xFF0F4D3A)
val SecondaryLightGreen = Color(0xFFE8F3F0)
val MediumGray = Color(0xFF6B7280)
val DarkBlack = Color(0xFF1F2937)
val PureWhite = Color(0xFFFFFFFF)
val LightGold = Color(0xFFD4AF37)
val DarkBg = Color(0xFF0F172A)
val DarkCardBg = Color(0xFF1F2937)

// 1. Emerald (الزمردي)
val EmeraldLight = lightColorScheme(
    primary = Color(0xFF047857), // Vibrant, radiant emerald
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF059669),
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = Color(0xFFF59E0B),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFF0FDF4),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF334155),
    outlineVariant = Color(0xFFE2E8F0)
)

val EmeraldDark = darkColorScheme(
    primary = Color(0xFF34D399), // Luminous bright emerald mint - pops with high contrast on dark
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFF6EE7B7),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF022C22),
    tertiary = Color(0xFFFDE047),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF334155)
)

// 2. Blue (الأزرق الملكي)
val BlueLight = lightColorScheme(
    primary = Color(0xFF0284C7), // Vibrant sky azure
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0284C7),
    onSecondary = PureWhite,
    tertiary = Color(0xFFF59E0B),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFF0F9FF),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF334155)
)

val BlueDark = darkColorScheme(
    primary = Color(0xFF38BDF8), // Luminous sky blue - crystal clear on dark
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF075985),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF082F49),
    tertiary = Color(0xFFFDE047),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

// 3. Gold (الذهبي الملكي)
val GoldLight = lightColorScheme(
    primary = Color(0xFFD97706), // Warm rich amber gold
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFFD97706),
    onSecondary = PureWhite,
    tertiary = Color(0xFFF59E0B),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFFFFBEB),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF334155)
)

val GoldDark = darkColorScheme(
    primary = Color(0xFFFBBF24), // Radiant warm gold - clear and shining on dark
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF451A03),
    tertiary = Color(0xFFFEF08A),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

// 4. Purple (الأرجواني/العنابي)
val PurpleLight = lightColorScheme(
    primary = Color(0xFFBE185D), // Radiant rose magenta
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFFCE7F3),
    onPrimaryContainer = Color(0xFF831843),
    secondary = Color(0xFFBE185D),
    onSecondary = PureWhite,
    tertiary = Color(0xFFF59E0B),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFFDF2F8),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF334155)
)

val PurpleDark = darkColorScheme(
    primary = Color(0xFFF472B6), // Luminous bright rose - vivid and clear on dark
    onPrimary = Color(0xFF500724),
    primaryContainer = Color(0xFF831843),
    onPrimaryContainer = Color(0xFFFBCFE8),
    secondary = Color(0xFFFB7185),
    onSecondary = Color(0xFF500724),
    tertiary = Color(0xFFFDE047),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

// 5. Turquoise (التركوازي)
val TurquoiseLight = lightColorScheme(
    primary = Color(0xFF0D9488), // Vivid turquoise teal
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = Color(0xFF0D9488),
    onSecondary = PureWhite,
    tertiary = Color(0xFFF59E0B),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFF0FDFA),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF334155)
)

val TurquoiseDark = darkColorScheme(
    primary = Color(0xFF2DD4BF), // Electric glowing turquoise/cyan
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = Color(0xFF14B8A6),
    onSecondary = Color(0xFF042F2E),
    tertiary = Color(0xFFFDE047),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

// 6. Bright Gold (الذهبي البراق)
val BrightGoldLight = lightColorScheme(
    primary = Color(0xFFD97706), // Rich amber gold
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFFF59E0B),
    onSecondary = PureWhite,
    tertiary = Color(0xFFFBBF24),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFFFFBEB),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF334155)
)

val BrightGoldDark = darkColorScheme(
    primary = Color(0xFFFDE047), // Radiant glowing golden yellow
    onPrimary = Color(0xFF422006),
    primaryContainer = Color(0xFF713F12),
    onPrimaryContainer = Color(0xFFFEF08A),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF422006),
    tertiary = Color(0xFFFEF9C3),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

// 7. Pale Grey (الرمادي الباهت)
val PaleGreyLight = lightColorScheme(
    primary = Color(0xFF334155), // Crisp modern slate
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF475569),
    onSecondary = PureWhite,
    tertiary = Color(0xFFF59E0B),
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFF8FAFC),
    onBackground = DarkBlack,
    onSurface = DarkBlack,
    onSurfaceVariant = Color(0xFF475569)
)

val PaleGreyDark = darkColorScheme(
    primary = Color(0xFFE2E8F0), // Bright platinum silver
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF334155),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFFCBD5E1),
    onSecondary = Color(0xFF0F172A),
    tertiary = Color(0xFFFDE047),
    background = DarkBg,
    surface = DarkCardBg,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    themePalette: String = "EMERALD",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themePalette.uppercase()) {
        "BLUE" -> if (darkTheme) BlueDark else BlueLight
        "GOLD" -> if (darkTheme) GoldDark else GoldLight
        "BRIGHT_GOLD" -> if (darkTheme) BrightGoldDark else BrightGoldLight
        "PALE_GREY" -> if (darkTheme) PaleGreyDark else PaleGreyLight
        "PURPLE" -> if (darkTheme) PurpleDark else PurpleLight
        "TURQUOISE" -> if (darkTheme) TurquoiseDark else TurquoiseLight
        else -> if (darkTheme) EmeraldDark else EmeraldLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = CairoFontFamily)
        ) {
            ProvideTextStyle(value = TextStyle(fontFamily = CairoFontFamily)) {
                content()
            }
        }
    }
}
