package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object ClickSoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    fun playClick() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (e: Exception) {
            Log.e("ClickSoundPlayer", "Error playing click sound: ${e.message}")
        }
    }
}
