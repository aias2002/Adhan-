package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PrayerOffsets
import com.example.ui.theme.CairoFontFamily

@Composable
fun PrayerOffsetsDialog(
    currentOffsets: PrayerOffsets,
    appLanguage: String = "ar",
    onDismiss: () -> Unit,
    onSaveOffsets: (PrayerOffsets) -> Unit
) {
    val isAr = appLanguage != "en"

    var fajrOffset by remember { mutableIntStateOf(currentOffsets.fajr) }
    var sunriseOffset by remember { mutableIntStateOf(currentOffsets.sunrise) }
    var dhuhrOffset by remember { mutableIntStateOf(currentOffsets.dhuhr) }
    var asrOffset by remember { mutableIntStateOf(currentOffsets.asr) }
    var maghribOffset by remember { mutableIntStateOf(currentOffsets.maghrib) }
    var ishaOffset by remember { mutableIntStateOf(currentOffsets.isha) }

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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = if (isAr) "ضبط الدقائق" else "Fine-Tune Prayer Times",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("btn_close_offsets_dialog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isAr) "إغلاق" else "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (isAr) "تعديل الدقائق لكل صلاة:" else "Adjust the minute offset for each prayer:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Offset Stepper Rows
                        OffsetStepperRow(
                            prayerName = if (isAr) "الفجر" else "Fajr",
                            value = fajrOffset,
                            isAr = isAr,
                            onValueChange = { fajrOffset = it }
                        )

                        OffsetStepperRow(
                            prayerName = if (isAr) "الشروق" else "Sunrise",
                            value = sunriseOffset,
                            isAr = isAr,
                            onValueChange = { sunriseOffset = it }
                        )

                        OffsetStepperRow(
                            prayerName = if (isAr) "الظهر" else "Dhuhr",
                            value = dhuhrOffset,
                            isAr = isAr,
                            onValueChange = { dhuhrOffset = it }
                        )

                        OffsetStepperRow(
                            prayerName = if (isAr) "العصر" else "Asr",
                            value = asrOffset,
                            isAr = isAr,
                            onValueChange = { asrOffset = it }
                        )

                        OffsetStepperRow(
                            prayerName = if (isAr) "المغرب" else "Maghrib",
                            value = maghribOffset,
                            isAr = isAr,
                            onValueChange = { maghribOffset = it }
                        )

                        OffsetStepperRow(
                            prayerName = if (isAr) "العشاء" else "Isha",
                            value = ishaOffset,
                            isAr = isAr,
                            onValueChange = { ishaOffset = it }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Save Button
                        Button(
                            onClick = {
                                val updated = PrayerOffsets(
                                    fajr = fajrOffset,
                                    sunrise = sunriseOffset,
                                    dhuhr = dhuhrOffset,
                                    asr = asrOffset,
                                    maghrib = maghribOffset,
                                    isha = ishaOffset
                                )
                                onSaveOffsets(updated)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_offsets"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (isAr) "حفظ وتحديث" else "Save & Apply",
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
private fun OffsetStepperRow(
    prayerName: String,
    value: Int,
    isAr: Boolean,
    onValueChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayerName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Minus Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onValueChange((value - 1).coerceAtLeast(-60)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = if (isAr) "إنقاص" else "Decrease",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Current Value Text
                val formattedVal = when {
                    value > 0 -> "+$value ${if (isAr) "دقيقة" else "min"}"
                    value < 0 -> "$value ${if (isAr) "دقيقة" else "min"}"
                    else -> "0 ${if (isAr) "دقيقة" else "min"}"
                }
                Text(
                    text = formattedVal,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("txt_offset_$prayerName")
                )

                // Plus Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onValueChange((value + 1).coerceAtMost(60)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isAr) "زيادة" else "Increase",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
