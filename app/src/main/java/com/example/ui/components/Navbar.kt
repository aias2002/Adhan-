package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.LocalAppThemeTokens
import com.example.ui.util.AppLocalization

@Composable
fun AppNavbar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDrawer: () -> Unit,
    appLanguage: String = "ar",
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppThemeTokens.current
    val navBg = tokens.tile
    val borderColor = tokens.tileBorder

    val systemDensity = LocalDensity.current
    // Requirement 7: Cap fontScale at 1.4 for the navbar to prevent extreme distortion
    val cappedDensity = remember(systemDensity) {
        Density(
            density = systemDensity.density,
            fontScale = systemDensity.fontScale.coerceAtMost(1.4f)
        )
    }
    // Requirement 6: When fontScale > 1.3, only the active item shows text, others show icons only
    val isFontScaleLarge = systemDensity.fontScale > 1.3f

    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(navBg)
                .border(1.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(horizontal = 4.dp, vertical = 5.dp)
                .heightIn(min = 54.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabButton(
                    label = AppLocalization.getTabName(AppTab.PRAYER_CLOCK, appLanguage),
                    icon = Icons.Default.Schedule,
                    isSelected = currentTab == AppTab.PRAYER_CLOCK,
                    onClick = { onTabSelected(AppTab.PRAYER_CLOCK) },
                    testTag = "tab_prayer_clock",
                    isFontScaleLarge = isFontScaleLarge,
                    modifier = Modifier.weight(1f)
                )

                NavTabButton(
                    label = AppLocalization.getTabName(AppTab.PRAYER_LIST, appLanguage),
                    icon = Icons.Default.FormatListBulleted,
                    isSelected = currentTab == AppTab.PRAYER_LIST,
                    onClick = { onTabSelected(AppTab.PRAYER_LIST) },
                    testTag = "tab_prayer_list",
                    isFontScaleLarge = isFontScaleLarge,
                    modifier = Modifier.weight(1f)
                )

                NavTabButton(
                    label = AppLocalization.getTabName(AppTab.QURAN, appLanguage),
                    icon = Icons.Default.Book,
                    isSelected = currentTab == AppTab.QURAN,
                    onClick = { onTabSelected(AppTab.QURAN) },
                    testTag = "tab_quran",
                    isFontScaleLarge = isFontScaleLarge,
                    modifier = Modifier.weight(1f)
                )

                NavTabButton(
                    label = AppLocalization.getTabName(AppTab.QIBLA, appLanguage),
                    icon = Icons.Default.Explore,
                    isSelected = currentTab == AppTab.QIBLA,
                    onClick = { onTabSelected(AppTab.QIBLA) },
                    testTag = "tab_qibla",
                    isFontScaleLarge = isFontScaleLarge,
                    modifier = Modifier.weight(1f)
                )

                NavTabButton(
                    label = AppLocalization.getTabName(AppTab.HOLIDAYS, appLanguage),
                    icon = Icons.Default.CalendarMonth,
                    isSelected = currentTab == AppTab.HOLIDAYS,
                    onClick = { onTabSelected(AppTab.HOLIDAYS) },
                    testTag = "tab_holidays",
                    isFontScaleLarge = isFontScaleLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    isFontScaleLarge: Boolean,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppThemeTokens.current
    val activeColor = tokens.navActText
    val inactiveColor = tokens.ink.copy(alpha = 0.65f)
    val activeBg = tokens.navActBg

    val bgColor by animateColorAsState(
        if (isSelected) activeBg else Color.Transparent,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "tab_bg"
    )
    val contentColor by animateColorAsState(
        if (isSelected) activeColor else inactiveColor,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "tab_color"
    )

    // When fontScale > 1.3: only show text if selected; otherwise keep icons only
    val showLabel = !isFontScaleLarge || isSelected

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = if (showLabel) 6.dp else 10.dp, horizontal = 2.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            if (showLabel) {
                Spacer(modifier = Modifier.height(2.dp))
                AutoSizingText(
                    text = label,
                    color = contentColor,
                    targetFontSize = 13.5.sp,
                    minFontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = CairoFontFamily,
                    textAlign = TextAlign.Center,
                    lineHeightMultiplier = 1.3f,
                    maxLines = 1
                )
            }
        }
    }
}
