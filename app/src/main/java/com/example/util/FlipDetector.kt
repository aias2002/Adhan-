package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class FlipDetector(
    context: Context,
    private val onFlipDown: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var isListening = false
    private var startListeningTime = 0L
    private var lastTriggerTime = 0L

    fun startListening() {
        if (!isListening && accelerometer != null) {
            startListeningTime = System.currentTimeMillis()
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                sensorManager?.unregisterListener(this)
            } catch (_: Exception) {}
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            val now = System.currentTimeMillis()
            // Ignore initial 3 seconds so the adhan is not immediately muted if phone is face-down on table
            if (now - startListeningTime < 3000L) {
                return
            }
            // When phone is turned face down (z < -7.0 m/s²)
            if (z < -7.0f && (now - lastTriggerTime > 2000L)) {
                lastTriggerTime = now
                onFlipDown()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
