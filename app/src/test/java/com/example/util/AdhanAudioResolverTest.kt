package com.example.util

import com.example.data.model.PRESET_ADHAN_SOUNDS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhanAudioResolverTest {

    @Test
    fun testTathwibAvailabilityCheck() {
        // Both Makkah and Madinah have authentic Tathwib recordings bundled
        assertTrue(AdhanAudioResolver.hasTathwib("makkah"))
        assertTrue(AdhanAudioResolver.hasTathwib("madinah"))
    }

    @Test
    fun testFajrPrayerWithTathwib() {
        // Makkah with Tathwib -> plays raw_makkah_fajr
        assertEquals(
            "raw_makkah_fajr",
            AdhanAudioResolver.resolveAudioTarget(
                prayerKey = "fajr",
                selectedSoundId = "makkah",
                fajrWithTatweeb = true
            )
        )

        // Madinah with Tathwib -> plays raw_madinah_fajr
        assertEquals(
            "raw_madinah_fajr",
            AdhanAudioResolver.resolveAudioTarget(
                prayerKey = "fajr",
                selectedSoundId = "madinah",
                fajrWithTatweeb = true
            )
        )
    }

    @Test
    fun testFajrPrayerWithStandardFormula() {
        // Makkah with Standard formula -> plays raw_makkah
        assertEquals(
            "raw_makkah",
            AdhanAudioResolver.resolveAudioTarget(
                prayerKey = "fajr",
                selectedSoundId = "makkah",
                fajrWithTatweeb = false
            )
        )

        // Madinah with Standard formula -> plays raw_madinah
        assertEquals(
            "raw_madinah",
            AdhanAudioResolver.resolveAudioTarget(
                prayerKey = "fajr",
                selectedSoundId = "madinah",
                fajrWithTatweeb = false
            )
        )
    }

    @Test
    fun testNonFajrPrayersNeverPlayTathwib() {
        val nonFajrPrayers = listOf("dhuhr", "asr", "maghrib", "isha")

        for (prayer in nonFajrPrayers) {
            // Non-fajr prayers ALWAYS play standard adhan (raw_makkah or raw_madinah)
            assertEquals(
                "raw_makkah",
                AdhanAudioResolver.resolveAudioTarget(
                    prayerKey = prayer,
                    selectedSoundId = "makkah",
                    fajrWithTatweeb = true
                )
            )

            assertEquals(
                "raw_madinah",
                AdhanAudioResolver.resolveAudioTarget(
                    prayerKey = prayer,
                    selectedSoundId = "madinah",
                    fajrWithTatweeb = true
                )
            )
        }
    }

    @Test
    fun testSunriseResolution() {
        assertEquals(
            "raw_rooster",
            AdhanAudioResolver.resolveAudioTarget(
                prayerKey = "sunrise",
                isSunrise = true,
                useRoosterForSunrise = true
            )
        )

        assertEquals(
            "raw_makkah",
            AdhanAudioResolver.resolveAudioTarget(
                prayerKey = "sunrise",
                isSunrise = true,
                useRoosterForSunrise = false
            )
        )
    }
}
