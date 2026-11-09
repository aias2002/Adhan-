package com.example.util

import android.content.Context
import com.example.data.model.PRESET_ADHAN_SOUNDS

/**
 * Single Source of Truth for Adhan Audio Resolution.
 * Resolves the appropriate audio recording based on:
 * - Prayer identity (Fajr vs. other 4 prayers vs. Sunrise)
 * - Muezzin selection
 * - Fajr formula (with Tathwib vs. Standard)
 *
 * Enforces strict rules:
 * 1. Tathwib ("الصلاة خير من النوم") belongs strictly to Fajr prayer.
 *    Any other prayer (Dhuhr, Asr, Maghrib, Isha) ALWAYS plays standard formula.
 * 2. If a muezzin does not have a recorded Tathwib clip, plays their authentic standard adhan
 *    and NEVER secretly swaps their voice with another muezzin.
 * 3. Consistent behavior across previews, actual alarms, notifications, and tests.
 */
object AdhanAudioResolver {

    /**
     * Checks if the given muezzin preset has an authentic recording containing Tathwib.
     */
    fun hasTathwib(soundId: String?): Boolean {
        if (soundId == null) return false
        val preset = PRESET_ADHAN_SOUNDS.find { it.id == soundId }
        return preset?.hasTathwib == true
    }

    /**
     * Resolves the exact audio target identifier or URI to be played.
     */
    fun resolveAudioTarget(
        prayerKey: String,
        prayerName: String = "",
        selectedSoundId: String = "makkah",
        selectedFajrSoundId: String = selectedSoundId,
        customSoundUri: String = "",
        fajrWithTatweeb: Boolean = true,
        isSunrise: Boolean = false,
        useRoosterForSunrise: Boolean = true
    ): String {
        if (isSunrise) {
            return if (useRoosterForSunrise) "raw_rooster" else "raw_makkah"
        }

        val normalizedKey = prayerKey.trim().lowercase()
        val isFajr = normalizedKey == "fajr" ||
                (prayerName.contains("الفجر") &&
                 !prayerName.contains("الظهر") &&
                 !prayerName.contains("العصر") &&
                 !prayerName.contains("المغرب") &&
                 !prayerName.contains("العشاء"))

        return if (isFajr) {
            val effectiveFajrId = if (selectedFajrSoundId.isNotEmpty()) selectedFajrSoundId else selectedSoundId
            if (effectiveFajrId == "custom_local" && customSoundUri.isNotEmpty()) {
                customSoundUri
            } else {
                val preset = PRESET_ADHAN_SOUNDS.find { it.id == effectiveFajrId } ?: PRESET_ADHAN_SOUNDS.first()
                if (fajrWithTatweeb) {
                    if (preset.hasTathwib) {
                        preset.fajrAudioUrl
                    } else {
                        // No authentic Tathwib recording available -> Play muezzin's standard recording
                        preset.audioUrl
                    }
                } else {
                    // Standard formula requested -> Always play standard recording (no Tathwib)
                    preset.audioUrl
                }
            }
        } else {
            // Other prayers -> Strictly standard formula
            if (selectedSoundId == "custom_local" && customSoundUri.isNotEmpty()) {
                customSoundUri
            } else {
                val preset = PRESET_ADHAN_SOUNDS.find { it.id == selectedSoundId } ?: PRESET_ADHAN_SOUNDS.first()
                preset.audioUrl
            }
        }
    }

    /**
     * Convenience method to resolve audio target directly from SharedPreferences.
     * Robustly checks both scheduler prefs and app settings prefs for both legacy and current key names.
     */
    fun resolveFromPrefs(
        context: Context,
        prayerKey: String,
        prayerName: String = "",
        isSunrise: Boolean = false
    ): String {
        val schedulerPrefs = AdhanScheduler.getPrefs(context)
        val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

        var selectedSoundId = schedulerPrefs.getString("selected_sound_id", null)
            ?: schedulerPrefs.getString("selected_adhan_id", null)
            ?: appPrefs.getString("selected_sound_id", null)
            ?: appPrefs.getString("selected_adhan_id", "makkah")
            ?: "makkah"

        if (selectedSoundId == "algeria" || selectedSoundId == "riad_algeria") {
            selectedSoundId = "makkah"
        }

        var selectedFajrSoundId = schedulerPrefs.getString("selected_fajr_sound_id", null)
            ?: schedulerPrefs.getString("selected_fajr_adhan_id", null)
            ?: appPrefs.getString("selected_fajr_sound_id", null)
            ?: appPrefs.getString("selected_fajr_adhan_id", selectedSoundId)
            ?: selectedSoundId

        if (selectedFajrSoundId == "algeria" || selectedFajrSoundId == "riad_algeria") {
            selectedFajrSoundId = "makkah"
        }

        val customSoundUri = schedulerPrefs.getString("custom_sound_uri", null)
            ?: appPrefs.getString("custom_sound_uri", "")
            ?: ""

        val fajrWithTatweeb = if (schedulerPrefs.contains("fajr_formula_tatweeb")) {
            schedulerPrefs.getBoolean("fajr_formula_tatweeb", true)
        } else {
            appPrefs.getBoolean("fajr_formula_tatweeb", true)
        }

        val useRooster = if (schedulerPrefs.contains("use_rooster_for_sunrise")) {
            schedulerPrefs.getBoolean("use_rooster_for_sunrise", true)
        } else {
            appPrefs.getBoolean("use_rooster_for_sunrise", true)
        }

        return resolveAudioTarget(
            prayerKey = prayerKey,
            prayerName = prayerName,
            selectedSoundId = selectedSoundId,
            selectedFajrSoundId = selectedFajrSoundId,
            customSoundUri = customSoundUri,
            fajrWithTatweeb = fajrWithTatweeb,
            isSunrise = isSunrise,
            useRoosterForSunrise = useRooster
        )
    }
}
