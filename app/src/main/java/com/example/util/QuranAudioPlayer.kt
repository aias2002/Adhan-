package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    var onAyahCompleted: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onProgressUpdate: ((positionMs: Int, durationMs: Int) -> Unit)? = null

    private val progressHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        val pos = mp.currentPosition
                        val dur = mp.duration
                        onProgressUpdate?.invoke(pos, dur)
                        progressHandler.postDelayed(this, 80)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressTracking() {
        progressHandler.removeCallbacks(progressRunnable)
    }

    fun playAyah(
        reciterIdentifier: String,
        surahNumber: Int,
        ayahNumberInSurah: Int,
        onCompletion: () -> Unit
    ) {
        stop()

        onAyahCompleted = onCompletion

        // Check if file is downloaded locally (Internal or SD card)
        val fileInternal = QuranStorageManager.getLocalAudioFile(context, false, reciterIdentifier, surahNumber, ayahNumberInSurah)
        val fileSd = QuranStorageManager.getLocalAudioFile(context, true, reciterIdentifier, surahNumber, ayahNumberInSurah)

        val localFile = when {
            fileInternal != null && fileInternal.exists() && fileInternal.length() > 1000 -> fileInternal
            fileSd != null && fileSd.exists() && fileSd.length() > 1000 -> fileSd
            else -> null
        }

        if (localFile != null) {
            playFromUrl(localFile.absolutePath, null)
        } else {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val errorMsg = "لا يتوفر اتصال بالإنترنت لتشغيل التلاوة. يرجى الاتصال بالإنترنت أو تنزيل السورة للاستماع في وضع عدم الاتصال."
                onError?.invoke(errorMsg)
                return
            }

            val folder = RECITER_FOLDER_MAP[reciterIdentifier] ?: "Alafasy_128kbps"
            val formattedSurah = String.format(java.util.Locale.US, "%03d", surahNumber)
            val formattedAyah = String.format(java.util.Locale.US, "%03d", ayahNumberInSurah)

            val primaryUrl = "https://everyayah.com/data/$folder/$formattedSurah$formattedAyah.mp3"
            val backupUrl = if (folder.contains("/")) {
                null
            } else {
                "https://mirrors.quranicaudio.com/everyayah/$folder/$formattedSurah$formattedAyah.mp3"
            }

            playFromUrl(primaryUrl, backupUrl)
        }
    }

    private fun playFromUrl(url: String, fallbackUrl: String? = null) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    startProgressTracking()
                }
                setOnCompletionListener {
                    stopProgressTracking()
                    onProgressUpdate?.invoke(0, 0)
                    onAyahCompleted?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressTracking()
                    onProgressUpdate?.invoke(0, 0)
                    if (fallbackUrl != null) {
                        Log.w("QuranAudioPlayer", "Primary audio failed, switching to backup mirror...")
                        stop()
                        playFromUrl(fallbackUrl, null)
                    } else {
                        val errorMsg = if (!NetworkUtils.isNetworkAvailable(context)) {
                            "انقطع الاتصال بالإنترنت أثناء تشغيل التلاوة. يرجى إعادة الاتصال."
                        } else {
                            "تعذر تشغيل تلاوة الآية الصوتية، يرجى التحقق من الاتصال بالإنترنت."
                        }
                        onError?.invoke(errorMsg)
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            stopProgressTracking()
            onProgressUpdate?.invoke(0, 0)
            Log.e("QuranAudioPlayer", "Error playing ayah: ${e.message}")
            if (fallbackUrl != null) {
                playFromUrl(fallbackUrl, null)
            } else {
                val errorMsg = if (!NetworkUtils.isNetworkAvailable(context)) {
                    "لا يتوفر اتصال بالإنترنت لتشغيل التلاوة"
                } else {
                    "خطأ في تشغيل الصوت: ${e.message}"
                }
                onError?.invoke(errorMsg)
            }
        }
    }

    fun stop() {
        stopProgressTracking()
        onProgressUpdate?.invoke(0, 0)
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            Log.e("QuranAudioPlayer", "Error stopping player: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    fun pause() {
        stopProgressTracking()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                startProgressTracking()
            }
        } catch (_: Exception) {}
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }
}

data class ReciterInfo(
    val id: String,
    val nameAr: String,
    val styleAr: String
)

val RECITER_FOLDER_MAP = mapOf(
    "ar.alijaber" to "Ali_Jaber_64kbps",
    "ar.minshawi" to "Minshawy_Murattal_128kbps",
    "ar.minshawimujawwad" to "Minshawy_Mujawwad_192kbps",
    "ar.abdulbasitmurattal" to "Abdul_Basit_Murattal_192kbps",
    "ar.husary" to "Husary_128kbps",
    "ar.ghamadi" to "Ghamadi_40kbps",
    "ar.ayyoub" to "Muhammad_Ayyoub_128kbps",
    "ar.bukhatir" to "Salaah_AbdulRahman_Bukhatir_128kbps",
    "ar.shuraym" to "Saood_ash-Shuraym_128kbps",
    "ar.mahermuaiqly" to "MaherAlMuaiqly128kbps",
    "ar.juhaynee" to "Abdullaah_3awwaad_Al-Juhaynee_128kbps",
    "ar.alafasy" to "Alafasy_128kbps",
    "ar.dussary" to "Yasser_Ad-Dussary_128kbps",
    "ar.nasser" to "Nasser_Alqatami_128kbps",
    "ar.yassinjazaery" to "warsh/warsh_yassin_al_jazaery_64kbps"
)

val PRESET_RECITERS = listOf(
    ReciterInfo("ar.alijaber", "الشيخ علي عبد الله جابر", "إمام المسجد الحرام"),
    ReciterInfo("ar.minshawi", "الشيخ محمد صديق المنشاوي", "المرتل الخاشع"),
    ReciterInfo("ar.minshawimujawwad", "الشيخ محمد صديق المنشاوي", "المجود الباكي"),
    ReciterInfo("ar.abdulbasitmurattal", "الشيخ عبد الباسط عبد الصمد", "مرتل خاشع"),
    ReciterInfo("ar.husary", "الشيخ محمود خليل الحصري", "المصحف المعلم (ترتيل)"),
    ReciterInfo("ar.ayyoub", "الشيخ محمد أيوب", "إمام المسجد النبوي"),
    ReciterInfo("ar.bukhatir", "الشيخ صلاح بو خاطر", "تلاوة خاشعة مرسلة"),
    ReciterInfo("ar.shuraym", "الشيخ سعود الشريم", "إمام الحرم المكي السابق"),
    ReciterInfo("ar.mahermuaiqly", "الشيخ ماهر المعيقلي", "إمام الحرم المكي"),
    ReciterInfo("ar.juhaynee", "الشيخ عبد الله عواد الجهني", "إمام الحرم المكي"),
    ReciterInfo("ar.dussary", "الشيخ ياسر الدوسري", "إمام الحرم المكي"),
    ReciterInfo("ar.nasser", "الشيخ ناصر القطامي", "تلاوة مؤثرة"),
    ReciterInfo("ar.ghamadi", "الشيخ سعد بن سعيد الغامدي", "مرتل خاشع"),
    ReciterInfo("ar.yassinjazaery", "القارئ ياسين الجزائري", "برواية ورش عن نافع")
)
