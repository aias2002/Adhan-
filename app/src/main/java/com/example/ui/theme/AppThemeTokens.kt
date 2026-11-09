package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeTokens(
    val id: String,
    val nameAr: String,
    val isDark: Boolean,
    val bg: Color,
    val card: Color,
    val accent: Color,
    val cardSub: Color,
    val colon: Color,
    val tile: Color,
    val tileBorder: Color,
    val tileName: Color,
    val tileTime: Color,
    val nextBg: Color,
    val nextName: Color,
    val nextTime: Color,
    val ink: Color,
    val navActBg: Color,
    val navActText: Color
)

// 1. زمردي فاتح
val EmeraldLightTokens = AppThemeTokens(
    id = "emerald_light",
    nameAr = "زمردي",
    isDark = false,
    bg = Color(0xFFFBF7EE),
    card = Color(0xFF0B3B2C),
    accent = Color(0xFFF5C84C),
    cardSub = Color(0xFFCFE3D8),
    colon = Color(0xFF78A18F),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFE3DAC3),
    tileName = Color(0xFF4A5A52),
    tileTime = Color(0xFF10261E),
    nextBg = Color(0xFFF5C84C),
    nextName = Color(0xFF3A2A00),
    nextTime = Color(0xFF1F1500),
    ink = Color(0xFF10261E),
    navActBg = Color(0xFFDCEFE4),
    navActText = Color(0xFF0B3B2C)
)

// 2. زمردي داكن
val EmeraldDarkTokens = AppThemeTokens(
    id = "emerald_dark",
    nameAr = "زمردي",
    isDark = true,
    bg = Color(0xFF08140F),
    card = Color(0xFF0F3D2E),
    accent = Color(0xFFFFD35C),
    cardSub = Color(0xFFBFE0D0),
    colon = Color(0xFF6FA68F),
    tile = Color(0xFF12201A),
    tileBorder = Color(0xFF2A4439),
    tileName = Color(0xFFA9C7B8),
    tileTime = Color(0xFFF2F7F4),
    nextBg = Color(0xFFFFD35C),
    nextName = Color(0xFF3A2A00),
    nextTime = Color(0xFF1F1500),
    ink = Color(0xFFF2F7F4),
    navActBg = Color(0xFF1D3A2E),
    navActText = Color(0xFFFFD35C)
)

// 3. ليلي فاتح
val NightLightTokens = AppThemeTokens(
    id = "night_light",
    nameAr = "ليلي",
    isDark = false,
    bg = Color(0xFFF2F5F9),
    card = Color(0xFF0C2340),
    accent = Color(0xFFFFC857),
    cardSub = Color(0xFFC6D4E6),
    colon = Color(0xFF7C93B3),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFD6DFEA),
    tileName = Color(0xFF4B5C78),
    tileTime = Color(0xFF0C2340),
    nextBg = Color(0xFFFFC857),
    nextName = Color(0xFF3A2A00),
    nextTime = Color(0xFF1F1500),
    ink = Color(0xFF0C2340),
    navActBg = Color(0xFFDCE7F5),
    navActText = Color(0xFF0C2340)
)

// 4. ليلي داكن
val NightDarkTokens = AppThemeTokens(
    id = "night_dark",
    nameAr = "ليلي",
    isDark = true,
    bg = Color(0xFF070F1C),
    card = Color(0xFF0C2340),
    accent = Color(0xFFFFC857),
    cardSub = Color(0xFFC6D4E6),
    colon = Color(0xFF7C93B3),
    tile = Color(0xFF101D33),
    tileBorder = Color(0xFF243552),
    tileName = Color(0xFFA9B8D0),
    tileTime = Color(0xFFF0F4FA),
    nextBg = Color(0xFFFFC857),
    nextName = Color(0xFF3A2A00),
    nextTime = Color(0xFF1F1500),
    ink = Color(0xFFF0F4FA),
    navActBg = Color(0xFF1B2E4D),
    navActText = Color(0xFFFFC857)
)

// 5. ذهبي صحراوي فاتح
val GoldLightTokens = AppThemeTokens(
    id = "gold_light",
    nameAr = "ذهبي",
    isDark = false,
    bg = Color(0xFFFAF6EE),
    card = Color(0xFF7A5200),
    accent = Color(0xFFFEF3C7),
    cardSub = Color(0xFFFDE68A),
    colon = Color(0xFFB45309),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFEADBBE),
    tileName = Color(0xFF6B5838),
    tileTime = Color(0xFF382503),
    nextBg = Color(0xFFD97706),
    nextName = Color(0xFFFFFFFF),
    nextTime = Color(0xFFFFFFFF),
    ink = Color(0xFF382503),
    navActBg = Color(0xFFFEF3C7),
    navActText = Color(0xFF7A5200)
)

// 6. ذهبي صحراوي داكن
val GoldDarkTokens = AppThemeTokens(
    id = "gold_dark",
    nameAr = "ذهبي",
    isDark = true,
    bg = Color(0xFF141008),
    card = Color(0xFF3D2A00),
    accent = Color(0xFFFBBF24),
    cardSub = Color(0xFFFDE68A),
    colon = Color(0xFFD97706),
    tile = Color(0xFF1F180B),
    tileBorder = Color(0xFF4A3714),
    tileName = Color(0xFFD4B886),
    tileTime = Color(0xFFFFF8E7),
    nextBg = Color(0xFFFBBF24),
    nextName = Color(0xFF261800),
    nextTime = Color(0xFF170E00),
    ink = Color(0xFFFFF8E7),
    navActBg = Color(0xFF382604),
    navActText = Color(0xFFFBBF24)
)

// 7. تركوازي فاتح
val TurquoiseLightTokens = AppThemeTokens(
    id = "turquoise_light",
    nameAr = "تركوازي",
    isDark = false,
    bg = Color(0xFFF0F9FF),
    card = Color(0xFF0D5C63),
    accent = Color(0xFF0284C7),
    cardSub = Color(0xFFBAE6FD),
    colon = Color(0xFF0284C7),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFBAE6FD),
    tileName = Color(0xFF336066),
    tileTime = Color(0xFF083338),
    nextBg = Color(0xFF0284C7),
    nextName = Color(0xFFFFFFFF),
    nextTime = Color(0xFFFFFFFF),
    ink = Color(0xFF083338),
    navActBg = Color(0xFFE0F2FE),
    navActText = Color(0xFF0D5C63)
)

// 8. تركوازي داكن
val TurquoiseDarkTokens = AppThemeTokens(
    id = "turquoise_dark",
    nameAr = "تركوازي",
    isDark = true,
    bg = Color(0xFF041217),
    card = Color(0xFF09393E),
    accent = Color(0xFF38BDF8),
    cardSub = Color(0xFFBAE6FD),
    colon = Color(0xFF0284C7),
    tile = Color(0xFF0B2128),
    tileBorder = Color(0xFF164E59),
    tileName = Color(0xFF90CAD6),
    tileTime = Color(0xFFF0F9FF),
    nextBg = Color(0xFF38BDF8),
    nextName = Color(0xFF02242B),
    nextTime = Color(0xFF01161B),
    ink = Color(0xFFF0F9FF),
    navActBg = Color(0xFF0C333D),
    navActText = Color(0xFF38BDF8)
)

// 9. أرجواني فاتح
val PurpleLightTokens = AppThemeTokens(
    id = "purple_light",
    nameAr = "أرجواني",
    isDark = false,
    bg = Color(0xFFFDF4F8),
    card = Color(0xFF4A1525),
    accent = Color(0xFFDB2777),
    cardSub = Color(0xFFFBCFE8),
    colon = Color(0xFFDB2777),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFFBCFE8),
    tileName = Color(0xFF6B3A4C),
    tileTime = Color(0xFF2B0A15),
    nextBg = Color(0xFFDB2777),
    nextName = Color(0xFFFFFFFF),
    nextTime = Color(0xFFFFFFFF),
    ink = Color(0xFF2B0A15),
    navActBg = Color(0xFFFCE7F3),
    navActText = Color(0xFF4A1525)
)

// 10. أرجواني داكن
val PurpleDarkTokens = AppThemeTokens(
    id = "purple_dark",
    nameAr = "أرجواني",
    isDark = true,
    bg = Color(0xFF14050B),
    card = Color(0xFF380E1B),
    accent = Color(0xFFF472B6),
    cardSub = Color(0xFFFBCFE8),
    colon = Color(0xFFBE185D),
    tile = Color(0xFF230C14),
    tileBorder = Color(0xFF50192B),
    tileName = Color(0xFFDB9EB9),
    tileTime = Color(0xFFFDF4F8),
    nextBg = Color(0xFFF472B6),
    nextName = Color(0xFF24040F),
    nextTime = Color(0xFF150208),
    ink = Color(0xFFFDF4F8),
    navActBg = Color(0xFF3E1220),
    navActText = Color(0xFFF472B6)
)

// 11. ذهبي براق فاتح
val BrightGoldLightTokens = AppThemeTokens(
    id = "bright_gold_light",
    nameAr = "ذهبي براق",
    isDark = false,
    bg = Color(0xFFFFFDF5),
    card = Color(0xFF7A5805),
    accent = Color(0xFFFFD700),
    cardSub = Color(0xFFFFF1BD),
    colon = Color(0xFFD4AF37),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFF3E5AB),
    tileName = Color(0xFF6D5212),
    tileTime = Color(0xFF2F2102),
    nextBg = Color(0xFFD4AF37),
    nextName = Color(0xFF2E2000),
    nextTime = Color(0xFF1C1300),
    ink = Color(0xFF2F2102),
    navActBg = Color(0xFFFFF3CD),
    navActText = Color(0xFF7A5805)
)

// 12. ذهبي براق داكن
val BrightGoldDarkTokens = AppThemeTokens(
    id = "bright_gold_dark",
    nameAr = "ذهبي براق",
    isDark = true,
    bg = Color(0xFF131005),
    card = Color(0xFF473302),
    accent = Color(0xFFFFD700),
    cardSub = Color(0xFFFFF1BD),
    colon = Color(0xFFD4AF37),
    tile = Color(0xFF241C07),
    tileBorder = Color(0xFF5E460D),
    tileName = Color(0xFFDEC37A),
    tileTime = Color(0xFFFFFDF5),
    nextBg = Color(0xFFFFD700),
    nextName = Color(0xFF261B00),
    nextTime = Color(0xFF171000),
    ink = Color(0xFFFFFDF5),
    navActBg = Color(0xFF3B2C05),
    navActText = Color(0xFFFFD700)
)

// 13. رمادي باهت فاتح
val PaleGreyLightTokens = AppThemeTokens(
    id = "pale_grey_light",
    nameAr = "رمادي باهت",
    isDark = false,
    bg = Color(0xFFF4F6F8),
    card = Color(0xFF334155),
    accent = Color(0xFF64748B),
    cardSub = Color(0xFFCBD5E1),
    colon = Color(0xFF64748B),
    tile = Color(0xFFFFFFFF),
    tileBorder = Color(0xFFE2E8F0),
    tileName = Color(0xFF475569),
    tileTime = Color(0xFF0F172A),
    nextBg = Color(0xFF64748B),
    nextName = Color(0xFFFFFFFF),
    nextTime = Color(0xFFFFFFFF),
    ink = Color(0xFF0F172A),
    navActBg = Color(0xFFE2E8F0),
    navActText = Color(0xFF334155)
)

// 14. رمادي باهت داكن
val PaleGreyDarkTokens = AppThemeTokens(
    id = "pale_grey_dark",
    nameAr = "رمادي باهت",
    isDark = true,
    bg = Color(0xFF0F141C),
    card = Color(0xFF1E293B),
    accent = Color(0xFFCBD5E1),
    cardSub = Color(0xFF94A3B8),
    colon = Color(0xFF64748B),
    tile = Color(0xFF151C28),
    tileBorder = Color(0xFF2D3C52),
    tileName = Color(0xFF94A3B8),
    tileTime = Color(0xFFF1F5F9),
    nextBg = Color(0xFF94A3B8),
    nextName = Color(0xFF0F172A),
    nextTime = Color(0xFF020617),
    ink = Color(0xFFF1F5F9),
    navActBg = Color(0xFF253347),
    navActText = Color(0xFFE2E8F0)
)

fun getAppThemeTokens(themeStyle: String, isDark: Boolean): AppThemeTokens {
    return when (themeStyle.lowercase()) {
        "night", "ليلي" -> if (isDark) NightDarkTokens else NightLightTokens
        "gold", "ذهبي" -> if (isDark) GoldDarkTokens else GoldLightTokens
        "turquoise", "تركوازي" -> if (isDark) TurquoiseDarkTokens else TurquoiseLightTokens
        "purple", "أرجواني" -> if (isDark) PurpleDarkTokens else PurpleLightTokens
        "bright_gold", "ذهبي براق" -> if (isDark) BrightGoldDarkTokens else BrightGoldLightTokens
        "pale_grey", "رمادي باهت" -> if (isDark) PaleGreyDarkTokens else PaleGreyLightTokens
        else -> if (isDark) EmeraldDarkTokens else EmeraldLightTokens
    }
}

val LocalAppThemeTokens = compositionLocalOf { EmeraldLightTokens }
