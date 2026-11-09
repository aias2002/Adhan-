package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object QuranStorageManager {
    fun getAudioDirectory(context: Context, useSdCard: Boolean): File? {
        val dirs = context.getExternalFilesDirs(null)
        val baseDir = if (useSdCard && dirs.size > 1 && dirs[1] != null) {
            dirs[1]
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
        if (baseDir == null) return null
        val audioDir = File(baseDir, "quran_audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return audioDir
    }

    fun getLocalAudioFile(context: Context, useSdCard: Boolean, reciterIdentifier: String, surahNumber: Int, ayahNumberInSurah: Int): File? {
        val baseAudioDir = getAudioDirectory(context, useSdCard) ?: return null
        val reciterDir = File(baseAudioDir, reciterIdentifier)
        if (!reciterDir.exists()) {
            reciterDir.mkdirs()
        }
        val formattedSurah = String.format(java.util.Locale.US, "%03d", surahNumber)
        val formattedAyah = String.format(java.util.Locale.US, "%03d", ayahNumberInSurah)
        return File(reciterDir, "$formattedSurah$formattedAyah.mp3")
    }

    fun isAudioFileDownloaded(context: Context, reciterIdentifier: String, surahNumber: Int, ayahNumberInSurah: Int): Boolean {
        val fileInternal = getLocalAudioFile(context, false, reciterIdentifier, surahNumber, ayahNumberInSurah)
        if (fileInternal != null && fileInternal.exists() && fileInternal.length() > 1000) return true

        val fileSd = getLocalAudioFile(context, true, reciterIdentifier, surahNumber, ayahNumberInSurah)
        if (fileSd != null && fileSd.exists() && fileSd.length() > 1000) return true

        return false
    }

    fun isSurahDownloaded(context: Context, reciterIdentifier: String, surahNumber: Int, totalAyahs: Int): Boolean {
        if (totalAyahs <= 0) return false
        for (i in 1..totalAyahs) {
            if (!isAudioFileDownloaded(context, reciterIdentifier, surahNumber, i)) {
                return false
            }
        }
        return true
    }

    /**
     * Efficiently scans reciter directory once in background to determine all fully downloaded surahs.
     */
    fun getDownloadedSurahNumbers(
        context: Context,
        reciterIdentifier: String,
        allSurahs: List<com.example.data.model.SurahItem>
    ): Set<Int> {
        val downloadedSet = mutableSetOf<Int>()
        try {
            val dirs = listOfNotNull(
                getAudioDirectory(context, false),
                if (hasSdCard(context)) getAudioDirectory(context, true) else null
            )

            val countsBySurah = mutableMapOf<Int, Int>()
            for (baseDir in dirs) {
                val reciterDir = File(baseDir, reciterIdentifier)
                if (reciterDir.exists() && reciterDir.isDirectory) {
                    val files = reciterDir.list() ?: continue
                    for (fileName in files) {
                        if (fileName.endsWith(".mp3") && fileName.length >= 6) {
                            val surahNum = fileName.take(3).toIntOrNull()
                            if (surahNum != null) {
                                countsBySurah[surahNum] = (countsBySurah[surahNum] ?: 0) + 1
                            }
                        }
                    }
                }
            }

            for (surah in allSurahs) {
                val count = countsBySurah[surah.number] ?: 0
                if (count >= surah.numberOfAyahs && surah.numberOfAyahs > 0) {
                    downloadedSet.add(surah.number)
                }
            }
        } catch (_: Exception) {}
        return downloadedSet
    }

    fun hasSdCard(context: Context): Boolean {
        val dirs = context.getExternalFilesDirs(null)
        return dirs.size > 1 && dirs[1] != null
    }

    fun getStorageFreeSpace(context: Context, useSdCard: Boolean): Long {
        val dirs = context.getExternalFilesDirs(null)
        val baseDir = if (useSdCard && dirs.size > 1 && dirs[1] != null) {
            dirs[1]
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
        return baseDir?.freeSpace ?: 0L
    }

    fun getStorageTotalSpace(context: Context, useSdCard: Boolean): Long {
        val dirs = context.getExternalFilesDirs(null)
        val baseDir = if (useSdCard && dirs.size > 1 && dirs[1] != null) {
            dirs[1]
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
        return baseDir?.totalSpace ?: 0L
    }

    suspend fun downloadAyah(
        context: Context,
        useSdCard: Boolean,
        reciterFolder: String,
        reciterIdentifier: String,
        surahNumber: Int,
        ayahNumberInSurah: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val destinationFile = getLocalAudioFile(context, useSdCard, reciterIdentifier, surahNumber, ayahNumberInSurah) ?: return@withContext false
        if (destinationFile.exists() && destinationFile.length() > 1000) return@withContext true

        val formattedSurah = String.format(java.util.Locale.US, "%03d", surahNumber)
        val formattedAyah = String.format(java.util.Locale.US, "%03d", ayahNumberInSurah)
        val urlString = "https://everyayah.com/data/$reciterFolder/$formattedSurah$formattedAyah.mp3"

        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.connect()

            val tempFile = File.createTempFile("ayah_dl", ".tmp", context.cacheDir)
            connection.getInputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.length() > 1000) {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
