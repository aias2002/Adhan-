package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: Int = 1, // Single active bookmark slot
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val edition: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_surahs")
data class FavoriteSurahEntity(
    @PrimaryKey val surahNumber: Int,
    val name: String,
    val englishName: String,
    val ayahsCount: Int
)

@Entity(tableName = "prayer_cache")
data class PrayerCacheEntity(
    @PrimaryKey val cityKey: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val dateHijri: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "surah_list_cache")
data class SurahCacheEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@Entity(tableName = "ayah_cache_table", primaryKeys = ["surahNumber", "numberInSurah", "edition"])
data class AyahCacheEntity(
    val surahNumber: Int,
    val numberInSurah: Int,
    val edition: String,
    val text: String,
    val juz: Int,
    val page: Int
)
