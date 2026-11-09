package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Auto-sizing single-line text that smoothly decreases font size when space is constrained,
 * with explicit lineHeight and centered lineHeightStyle to completely prevent descender/ascender clipping.
 */
@Composable
fun AutoSizingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    targetFontSize: TextUnit = 24.sp,
    minFontSize: TextUnit = 12.sp,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    lineHeightMultiplier: Float = 1.35f,
    fontFeatureSettings: String? = null,
    maxLines: Int = 1
) {
    var scaledFontSize by remember(text, targetFontSize) { mutableStateOf(targetFontSize) }
    var readyToDraw by remember(text, targetFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontSize = scaledFontSize,
        lineHeight = (scaledFontSize.value * lineHeightMultiplier).sp,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
            ),
            fontFeatureSettings = fontFeatureSettings
        ),
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && scaledFontSize.value > minFontSize.value) {
                val nextSize = (scaledFontSize.value - 1.0f).coerceAtLeast(minFontSize.value).sp
                if (nextSize != scaledFontSize) {
                    scaledFontSize = nextSize
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

/**
 * Auto-sizing text for AnnotatedString (e.g. countdown with colored segments).
 */
@Composable
fun AutoSizingAnnotatedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    targetFontSize: TextUnit = 34.sp,
    minFontSize: TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    lineHeightMultiplier: Float = 1.35f,
    fontFeatureSettings: String? = null,
    maxLines: Int = 1
) {
    var scaledFontSize by remember(text.text, targetFontSize) { mutableStateOf(targetFontSize) }
    var readyToDraw by remember(text.text, targetFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        fontSize = scaledFontSize,
        lineHeight = (scaledFontSize.value * lineHeightMultiplier).sp,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
            ),
            fontFeatureSettings = fontFeatureSettings
        ),
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && scaledFontSize.value > minFontSize.value) {
                val nextSize = (scaledFontSize.value - 1.0f).coerceAtLeast(minFontSize.value).sp
                if (nextSize != scaledFontSize) {
                    scaledFontSize = nextSize
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}
