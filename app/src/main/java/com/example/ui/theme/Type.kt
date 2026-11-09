package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

val CairoFontFamily = FontFamily(
    // 400 - Normal
    Font(R.font.cairo_regular, FontWeight.W400),
    Font(R.font.cairo_regular, FontWeight.Normal),

    // 600 - SemiBold
    Font(R.font.cairo_semibold, FontWeight.W600),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),

    // 700 - Bold
    Font(R.font.cairo_bold, FontWeight.W700),
    Font(R.font.cairo_bold, FontWeight.Bold),

    // 800 - ExtraBold
    Font(R.font.cairo_extrabold, FontWeight.W800),
    Font(R.font.cairo_extrabold, FontWeight.ExtraBold),

    // Mappings for unavailable weights to nearest available:
    // 100, 200, 300 -> 400 (Normal)
    Font(R.font.cairo_regular, FontWeight.W100),
    Font(R.font.cairo_regular, FontWeight.W200),
    Font(R.font.cairo_regular, FontWeight.W300),
    Font(R.font.cairo_regular, FontWeight.Light),
    Font(R.font.cairo_regular, FontWeight.ExtraLight),
    Font(R.font.cairo_regular, FontWeight.Thin),

    // 500 (Medium) -> 600 (SemiBold)
    Font(R.font.cairo_semibold, FontWeight.W500),
    Font(R.font.cairo_semibold, FontWeight.Medium),

    // 900 (Black) -> 800 (ExtraBold)
    Font(R.font.cairo_extrabold, FontWeight.W900),
    Font(R.font.cairo_extrabold, FontWeight.Black)
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = CairoFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = CairoFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = CairoFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = CairoFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = CairoFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = CairoFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = CairoFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = CairoFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = CairoFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = CairoFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = CairoFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = CairoFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = CairoFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = CairoFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = CairoFontFamily)
)
