package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    org.junit.Assert.assertTrue(
        appName == "Adhan+" || appName == "Adhan+ Quran & Prayer" || appName == "الأذان والمصحف"
    )
  }

  @Test
  fun `verify hijri holiday calculation when today is 25 Rabi al-Thani`() {
    val safeDateToday = com.example.ui.screens.safeHijrahDate(1448, 4, 25)
    val todayEpoch = safeDateToday.toEpochDay()
    
    // Mawlid is Month 3, Day 12
    val mawlidDateThisYear = com.example.ui.screens.safeHijrahDate(1448, 3, 12)
    val mawlidDiffThisYear = (mawlidDateThisYear.toEpochDay() - todayEpoch).toInt()
    // Mawlid must be in the past (negative diff)
    org.junit.Assert.assertTrue(mawlidDiffThisYear < 0)

    // Isra & Mi'raj is Month 7, Day 27
    val israDateThisYear = com.example.ui.screens.safeHijrahDate(1448, 7, 27)
    val israDiffThisYear = (israDateThisYear.toEpochDay() - todayEpoch).toInt()
    // Isra must be upcoming (positive diff)
    org.junit.Assert.assertTrue(israDiffThisYear > 0)
  }
}
