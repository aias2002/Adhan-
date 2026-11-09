package com.example.util

import android.content.Context
import com.example.R

/**
 * Helper to manage dynamic worldwide mosque backgrounds across:
 * 1. App prayer times screen (PrayersListScreen / PrayersScreen)
 * 2. Vertical prayer widget (VerticalPrayerWidgetProvider)
 */
object MosqueBackgroundHelper {

    data class MosqueInfo(
        val prayerKey: String,
        val nameAr: String,
        val nameEn: String,
        val countryAr: String,
        val countryEn: String,
        val drawableResId: Int
    )

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("show_mosque_backgrounds", true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("show_mosque_backgrounds", enabled).apply()
    }

    fun getMosqueForPrayer(prayerKeyOrName: String?): MosqueInfo {
        val normalized = when (prayerKeyOrName?.trim()?.lowercase()) {
            "fajr", "الفجر" -> "fajr"
            "sunrise", "الشروق" -> "sunrise"
            "dhuhr", "الظهر" -> "dhuhr"
            "asr", "العصر" -> "asr"
            "maghrib", "المغرب" -> "maghrib"
            "isha", "العشاء" -> "isha"
            else -> "dhuhr"
        }
        return when (normalized) {
            "fajr" -> MosqueInfo(
                prayerKey = "fajr",
                nameAr = "المسجد النبوي الشريف",
                nameEn = "The Prophet's Mosque",
                countryAr = "المدينة المنورة",
                countryEn = "Madinah",
                drawableResId = R.drawable.img_mosque_madinah
            )
            "sunrise" -> MosqueInfo(
                prayerKey = "sunrise",
                nameAr = "المسجد الأقصى وقبة الصخرة",
                nameEn = "Al-Aqsa & Dome of the Rock",
                countryAr = "القدس الشريف",
                countryEn = "Jerusalem",
                drawableResId = R.drawable.img_mosque_aqsa
            )
            "dhuhr" -> MosqueInfo(
                prayerKey = "dhuhr",
                nameAr = "جامع الجزائر الأعظم",
                nameEn = "Great Mosque of Algiers",
                countryAr = "الجزائر",
                countryEn = "Algeria",
                drawableResId = R.drawable.bg_algeria_mosque
            )
            "asr" -> MosqueInfo(
                prayerKey = "asr",
                nameAr = "جامع الشيخ زايد الكبير",
                nameEn = "Sheikh Zayed Grand Mosque",
                countryAr = "أبوظبي",
                countryEn = "Abu Dhabi",
                drawableResId = R.drawable.img_mosque_zayed
            )
            "maghrib" -> MosqueInfo(
                prayerKey = "maghrib",
                nameAr = "المسجد الحرام والكعبة المشرفة",
                nameEn = "Masjid al-Haram & Kaaba",
                countryAr = "مكة المكرمة",
                countryEn = "Makkah",
                drawableResId = R.drawable.img_mosque_makkah
            )
            "isha" -> MosqueInfo(
                prayerKey = "isha",
                nameAr = "المسجد الحرام والكعبة المشرفة",
                nameEn = "Masjid al-Haram & Kaaba",
                countryAr = "مكة المكرمة",
                countryEn = "Makkah",
                drawableResId = R.drawable.img_mosque_makkah
            )
            else -> MosqueInfo(
                prayerKey = "default",
                nameAr = "المسجد الحرام",
                nameEn = "Masjid al-Haram",
                countryAr = "مكة المكرمة",
                countryEn = "Makkah",
                drawableResId = R.drawable.img_mosque_makkah
            )
        }
    }
}
