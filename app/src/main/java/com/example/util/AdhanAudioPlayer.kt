package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.R

class AdhanAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var focusRequest: Any? = null // AudioFocusRequest on API 26+

    var currentPlayingUrl: String? = null
        private set

    fun playSound(
        audioUrlOrUri: String,
        overrideSilentMode: Boolean = true,
        streamType: String? = null,
        onCompletion: () -> Unit = {}
    ) {
        stopSound()
        try {
            val resolvedStreamType = streamType ?: try {
                AdhanScheduler.getPrefs(context).getString("adhan_audio_stream_type", "media") ?: "media"
            } catch (_: Exception) {
                "media"
            }

            requestAudioFocus(overrideSilentMode, resolvedStreamType)

            val rawRes = getRawResId(audioUrlOrUri)
            if (rawRes != null) {
                playLocalRaw(rawRes, overrideSilentMode, resolvedStreamType, audioUrlOrUri, onCompletion)
                return
            }

            val audioAttributes = buildAudioAttributes(overrideSilentMode, resolvedStreamType)

            val mp = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)

                if (audioUrlOrUri.startsWith("content://") || audioUrlOrUri.startsWith("file://")) {
                    setDataSource(context, Uri.parse(audioUrlOrUri))
                } else {
                    setDataSource(audioUrlOrUri)
                }

                setOnPreparedListener {
                    start()
                    currentPlayingUrl = audioUrlOrUri
                }

                setOnCompletionListener {
                    stopSound()
                    onCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AdhanAudioPlayer", "Playback error ($what, $extra), falling back to offline raw adhan")
                    playLocalRaw(R.raw.adhan_makkah, overrideSilentMode, resolvedStreamType, audioUrlOrUri, onCompletion)
                    true
                }

                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e("AdhanAudioPlayer", "Exception in playSound: ${e.message}")
            val resolvedStreamType = streamType ?: "media"
            playLocalRaw(R.raw.adhan_makkah, overrideSilentMode, resolvedStreamType, audioUrlOrUri, onCompletion)
        }
    }

    private fun getRawResId(urlOrUri: String): Int? {
        return when {
            urlOrUri == "raw_makkah_fajr" || urlOrUri.contains("makkah_fajr") -> R.raw.adhan_makkah_fajr
            urlOrUri == "raw_madinah_fajr" || urlOrUri.contains("madinah_fajr") -> R.raw.adhan_madinah_fajr
            urlOrUri == "raw_makkah" || urlOrUri.contains("adhan.mp3") -> R.raw.adhan_makkah
            urlOrUri == "raw_madinah" || urlOrUri.contains("Madani") || urlOrUri.contains("madinah") -> R.raw.adhan_madinah
            urlOrUri == "raw_default" || urlOrUri.isEmpty() -> R.raw.adhan_makkah
            urlOrUri == "raw_riad" || urlOrUri.contains("Riad") || urlOrUri.contains("riad") -> R.raw.adhan_makkah
            urlOrUri == "raw_fajr" || urlOrUri.contains("adhan_fajr") -> R.raw.adhan_makkah_fajr
            urlOrUri == "raw_rooster" -> R.raw.rooster_crow
            else -> null
        }
    }

    private fun playLocalRaw(
        rawResId: Int,
        overrideSilentMode: Boolean = true,
        streamType: String = "media",
        playingIdentifier: String = "raw_default",
        onCompletion: () -> Unit
    ) {
        stopSound()
        try {
            requestAudioFocus(overrideSilentMode, streamType)
            val audioAttributes = buildAudioAttributes(overrideSilentMode, streamType)
            val afd = context.resources.openRawResourceFd(rawResId) ?: return
            val mp = MediaPlayer()
            try {
                mp.setAudioAttributes(audioAttributes)
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                mp.setOnCompletionListener {
                    stopSound()
                    onCompletion()
                }
                mp.prepare()
                mp.start()
                mediaPlayer = mp
                currentPlayingUrl = playingIdentifier
            } finally {
                try {
                    afd.close()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e("AdhanAudioPlayer", "Failed to play raw resource: ${e.message}")
            stopSound()
        }
    }

    private fun requestAudioFocus(overrideSilentMode: Boolean, streamType: String = "media") {
        try {
            val isAlarm = streamType == "alarm"
            val targetStream = if (isAlarm) AudioManager.STREAM_ALARM else AudioManager.STREAM_MUSIC
            if (overrideSilentMode) {
                // If phone is in silent or volume is at 0, unmute to a comfortable audible level
                val currentVol = audioManager?.getStreamVolume(targetStream) ?: 0
                val maxVol = audioManager?.getStreamMaxVolume(targetStream) ?: 15
                if (currentVol == 0) {
                    val targetVol = (maxVol * 0.70f).toInt().coerceAtLeast(1)
                    audioManager?.setStreamVolume(targetStream, targetVol, 0)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = buildAudioAttributes(overrideSilentMode, streamType)
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener { /* Keep focus for adhan */ }
                    .build()
                focusRequest = req
                audioManager?.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(null, targetStream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            }
        } catch (e: Exception) {
            Log.e("AdhanAudioPlayer", "Failed to request audio focus: ${e.message}")
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = focusRequest as? AudioFocusRequest
                if (req != null) {
                    audioManager?.abandonAudioFocusRequest(req)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (_: Exception) {}
        focusRequest = null
    }

    private fun buildAudioAttributes(overrideSilentMode: Boolean, streamType: String = "media"): AudioAttributes {
        val isAlarm = streamType == "alarm"
        return AudioAttributes.Builder()
            .setContentType(if (isAlarm) AudioAttributes.CONTENT_TYPE_SONIFICATION else AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(if (isAlarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA)
            .build()
    }

    fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) { }
        mediaPlayer = null
        currentPlayingUrl = null
        abandonAudioFocus()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}
