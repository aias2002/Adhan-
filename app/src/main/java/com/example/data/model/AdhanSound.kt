package com.example.data.model

data class AdhanSound(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val audioUrl: String, // Standard Adhan for Dhuhr, Asr, Maghrib, Isha (No Tathweeb)
    val fajrAudioUrl: String = audioUrl, // Fajr Adhan with Tathweeb if available, otherwise audioUrl
    val hasTathwib: Boolean = false,
    val isCustom: Boolean = false
)

val PRESET_ADHAN_SOUNDS = listOf(
    AdhanSound(
        id = "makkah",
        nameAr = "آذان الحرم المكي الشريف (مكة المكرمة)",
        nameEn = "Makkah Haram Adhan (Offline)",
        audioUrl = "raw_makkah",
        fajrAudioUrl = "raw_makkah_fajr",
        hasTathwib = true
    ),
    AdhanSound(
        id = "madinah",
        nameAr = "آذان المسجد النبوي الشريف (المدينة المنورة)",
        nameEn = "Madinah Nabawi Adhan (Offline)",
        audioUrl = "raw_madinah",
        fajrAudioUrl = "raw_madinah_fajr",
        hasTathwib = true
    )
)
