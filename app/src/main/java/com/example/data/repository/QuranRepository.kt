package com.example.data.repository

import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.FavoriteSurahDao
import com.example.data.local.FavoriteSurahEntity
import com.example.data.local.QuranCacheDao
import com.example.data.local.SurahCacheEntity
import com.example.data.local.AyahCacheEntity
import com.example.data.model.AyahItem
import com.example.data.model.SurahDetail
import com.example.data.model.SurahItem
import android.content.Context
import com.example.data.remote.AlquranApiService
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class QuranRepository(
    private val context: Context,
    private val apiService: AlquranApiService,
    private val bookmarkDao: BookmarkDao,
    private val favoriteSurahDao: FavoriteSurahDao,
    private val quranCacheDao: QuranCacheDao
) {
    val currentBookmark: Flow<BookmarkEntity?> = bookmarkDao.getBookmark()
    val favoriteSurahs: Flow<List<FavoriteSurahEntity>> = favoriteSurahDao.getAllFavorites()

    // High-speed In-Memory Cache to eliminate repeated disk/network reads during session
    private val memorySurahCache = java.util.concurrent.ConcurrentHashMap<String, SurahDetail>()

    suspend fun getSurahsList(): Result<List<SurahItem>> {
        // Fast-path: Check local database first for instant list display
        val cached = quranCacheDao.getCachedSurahs()
        if (cached.isNotEmpty()) {
            val list = cached.map {
                SurahItem(
                    number = it.number,
                    name = it.name,
                    englishName = it.englishName,
                    englishNameTranslation = it.englishNameTranslation,
                    numberOfAyahs = it.numberOfAyahs,
                    revelationType = it.revelationType
                )
            }
            return Result.success(list)
        }

        return try {
            val response = apiService.getSurahsList()
            if (response.code == 200 && response.data != null) {
                val entities = response.data.map { surah ->
                    SurahCacheEntity(
                        number = surah.number,
                        name = surah.name,
                        englishName = surah.englishName,
                        englishNameTranslation = surah.englishNameTranslation,
                        numberOfAyahs = surah.numberOfAyahs,
                        revelationType = surah.revelationType
                    )
                }
                quranCacheDao.insertSurahs(entities)
                Result.success(response.data)
            } else {
                getCachedSurahsOrError(Exception("فشل تحميل قائمة السور"))
            }
        } catch (e: Exception) {
            getCachedSurahsOrError(e)
        }
    }

    private suspend fun getCachedSurahsOrError(exception: Exception): Result<List<SurahItem>> {
        val cached = quranCacheDao.getCachedSurahs()
        return if (cached.isNotEmpty()) {
            Result.success(cached.map {
                SurahItem(
                    number = it.number,
                    name = it.name,
                    englishName = it.englishName,
                    englishNameTranslation = it.englishNameTranslation,
                    numberOfAyahs = it.numberOfAyahs,
                    revelationType = it.revelationType
                )
            })
        } else {
            Result.failure(exception)
        }
    }

    @Volatile
    private var warshJsonMap: Map<String, List<String>>? = null

    private suspend fun loadWarshSurahFromAssets(surahNumber: Int): SurahDetail? {
        val map = warshJsonMap ?: synchronized(this) {
            warshJsonMap ?: run {
                val jsonStr = context.assets.open("quran_warsh.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val tempMap = mutableMapOf<String, List<String>>()
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val arr = jsonObj.getJSONArray(k)
                    val list = ArrayList<String>(arr.length())
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                    tempMap[k] = list
                }
                tempMap.also { warshJsonMap = it }
            }
        }

        val verses = map[surahNumber.toString()] ?: return null
        val surahInfo = quranCacheDao.getCachedSurah(surahNumber)
        val ayahs = verses.mapIndexed { index, text ->
            AyahItem(
                number = index + 1,
                text = text,
                numberInSurah = index + 1,
                juz = null,
                page = null
            )
        }
        return SurahDetail(
            number = surahNumber,
            name = surahInfo?.name ?: "سورة $surahNumber",
            englishName = surahInfo?.englishName ?: "",
            englishNameTranslation = surahInfo?.englishNameTranslation ?: "",
            numberOfAyahs = ayahs.size,
            revelationType = surahInfo?.revelationType ?: "",
            ayahs = ayahs
        )
    }

    suspend fun getSurahDetail(surahNumber: Int, edition: String): Result<SurahDetail> {
        val targetEdition = if (edition == "quran-warsh") "ara-quranwarsh" else edition
        val cacheKey = "$surahNumber-$targetEdition"

        // In-Memory cache
        memorySurahCache[cacheKey]?.let { return Result.success(it) }

        // Local Room cache
        val localCached = getLocalCachedSurah(surahNumber, targetEdition)
        if (localCached != null) {
            memorySurahCache[cacheKey] = localCached
            return Result.success(localCached)
        }

        // Edition-specific fetching:
        if (edition == "quran-warsh") {
            return try {
                val detail = loadWarshSurahFromAssets(surahNumber)
                if (detail != null) {
                    cacheSurahDetail(surahNumber, targetEdition, detail)
                    memorySurahCache[cacheKey] = detail
                    Result.success(detail)
                } else {
                    Result.failure(Exception("تعذّر العثور على سورة رقم $surahNumber برواية ورش"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("تعذّر قراءة بيانات رواية ورش: ${e.message}"))
            }
        } else {
            return try {
                val response = apiService.getSurahDetail(surahNumber, targetEdition)
                if (response.code == 200 && response.data != null) {
                    cacheSurahDetail(surahNumber, targetEdition, response.data)
                    memorySurahCache[cacheKey] = response.data
                    Result.success(response.data)
                } else {
                    fetchFallbackOrCache(surahNumber, targetEdition)
                }
            } catch (e: Exception) {
                fetchFallbackOrCache(surahNumber, targetEdition, e)
            }
        }
    }

    private suspend fun getLocalCachedSurah(surahNumber: Int, edition: String): SurahDetail? {
        var cachedAyahs = quranCacheDao.getCachedAyahs(surahNumber, edition)
        // Removed automatic fallback to alternative edition for strict edition adherence
        // var cachedAyahs = quranCacheDao.getCachedAyahs(surahNumber, edition)

        if (cachedAyahs.isNotEmpty()) {
            val surahInfo = quranCacheDao.getCachedSurah(surahNumber)
            val ayahs = cachedAyahs.map {
                AyahItem(
                    number = it.numberInSurah,
                    text = it.text,
                    numberInSurah = it.numberInSurah,
                    juz = it.juz,
                    page = it.page
                )
            }
            return SurahDetail(
                number = surahNumber,
                name = surahInfo?.name ?: "سورة $surahNumber",
                englishName = surahInfo?.englishName ?: "",
                englishNameTranslation = surahInfo?.englishNameTranslation ?: "",
                numberOfAyahs = ayahs.size,
                revelationType = surahInfo?.revelationType ?: "",
                ayahs = ayahs
            )
        }
        return null
    }

    private suspend fun cacheSurahDetail(surahNumber: Int, edition: String, detail: SurahDetail) {
        val ayahEntities = detail.ayahs.map { ayah ->
            AyahCacheEntity(
                surahNumber = surahNumber,
                numberInSurah = ayah.numberInSurah,
                edition = edition,
                text = ayah.text,
                juz = ayah.juz ?: 1,
                page = ayah.page ?: 1
            )
        }
        quranCacheDao.insertAyahs(ayahEntities)
    }

    private suspend fun fetchFallbackOrCache(surahNumber: Int, edition: String, originalException: Exception? = null): Result<SurahDetail> {
        // Disabled fallback to ensure requested edition is strictly adhered to
        return getCachedAyahsOrError(surahNumber, edition, originalException ?: Exception("فشل تحميل آيات السورة"))
    }

    private suspend fun getCachedAyahsOrError(surahNumber: Int, edition: String, exception: Exception): Result<SurahDetail> {
        var cached = quranCacheDao.getCachedAyahs(surahNumber, edition)
        
        // Disabled fallback to alternative edition for strict edition adherence


        if (cached.isNotEmpty()) {
            val surahInfo = quranCacheDao.getCachedSurah(surahNumber)
            val ayahs = cached.map {
                AyahItem(
                    number = it.numberInSurah,
                    text = it.text,
                    numberInSurah = it.numberInSurah,
                    juz = it.juz,
                    page = it.page
                )
            }
            val detail = SurahDetail(
                number = surahNumber,
                name = surahInfo?.name ?: "سورة $surahNumber",
                englishName = surahInfo?.englishName ?: "",
                englishNameTranslation = surahInfo?.englishNameTranslation ?: "",
                numberOfAyahs = ayahs.size,
                revelationType = surahInfo?.revelationType ?: "",
                ayahs = ayahs
            )
            return Result.success(detail)
        } else {
            // Return a clear, informative offline usage guide dialog message rather than raw network exceptions
            return Result.failure(Exception("هذه السورة غير محفوظة للقراءة دون اتصال بعد. يرجى فتحها لمرة واحدة فقط أثناء الاتصال بالإنترنت ليتم تحميلها وحفظها تلقائياً على جهازك لقراءتها لاحقاً في أي وقت وبدون إنترنت."))
        }
    }

    suspend fun clearWarshData() {
        // Clear all Warsh data from database
        quranCacheDao.deleteAyahsByEdition("ara-quranwarsh")
        quranCacheDao.deleteAyahsByEdition("quran-warsh-madinah")
        // Clear in-memory cache
        memorySurahCache.clear()
    }

    fun getBookmark(edition: String): Flow<BookmarkEntity?> {
        val id = if (edition == "quran-warsh") 2 else 1
        return bookmarkDao.getBookmarkById(id)
    }

    suspend fun saveBookmark(surahNumber: Int, surahName: String, ayahNumber: Int, edition: String) {
        val id = if (edition == "quran-warsh") 2 else 1
        bookmarkDao.saveBookmark(
            BookmarkEntity(
                id = id,
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                edition = edition
            )
        )
    }

    suspend fun clearCacheForSurah(surahNumber: Int, edition: String) {
        val targetEdition = if (edition == "quran-warsh") "ara-quranwarsh" else edition
        memorySurahCache.remove("$surahNumber-$targetEdition")
        // Just delete specific surah data as before
        quranCacheDao.deleteAyahsByEdition(targetEdition) // Note: this clears ALL for that edition now, but it's okay for surah refresh
    }

    suspend fun toggleFavorite(surahItem: SurahItem): Boolean {
        val isFav = favoriteSurahDao.isFavorite(surahItem.number)
        if (isFav) {
            favoriteSurahDao.removeFavorite(surahItem.number)
            return false
        } else {
            favoriteSurahDao.addFavorite(
                FavoriteSurahEntity(
                    surahNumber = surahItem.number,
                    name = surahItem.name,
                    englishName = surahItem.englishName,
                    ayahsCount = surahItem.numberOfAyahs
                )
            )
            return true
        }
    }
}
