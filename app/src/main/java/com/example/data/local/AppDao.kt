package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE id = 1 LIMIT 1")
    fun getBookmark(): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    fun getBookmarkById(id: Int): Flow<BookmarkEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBookmark(bookmark: BookmarkEntity)
}

@Dao
interface FavoriteSurahDao {
    @Query("SELECT * FROM favorite_surahs")
    fun getAllFavorites(): Flow<List<FavoriteSurahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteSurahEntity)

    @Query("DELETE FROM favorite_surahs WHERE surahNumber = :surahNumber")
    suspend fun removeFavorite(surahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_surahs WHERE surahNumber = :surahNumber)")
    suspend fun isFavorite(surahNumber: Int): Boolean
}

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM prayer_cache WHERE cityKey = :cityKey LIMIT 1")
    suspend fun getCachedPrayer(cityKey: String): PrayerCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrayerCache(cache: PrayerCacheEntity)
}

@Dao
interface QuranCacheDao {
    @Query("SELECT * FROM surah_list_cache ORDER BY number ASC")
    suspend fun getCachedSurahs(): List<SurahCacheEntity>

    @Query("SELECT * FROM surah_list_cache WHERE number = :surahNumber LIMIT 1")
    suspend fun getCachedSurah(surahNumber: Int): SurahCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahCacheEntity>)

    @Query("SELECT * FROM ayah_cache_table WHERE surahNumber = :surahNumber AND edition = :edition ORDER BY numberInSurah ASC")
    suspend fun getCachedAyahs(surahNumber: Int, edition: String): List<AyahCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahCacheEntity>)

    @Query("DELETE FROM ayah_cache_table WHERE edition = :edition")
    suspend fun deleteAyahsByEdition(edition: String)
}
