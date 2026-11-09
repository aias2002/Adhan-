package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

class CompassSensorManager(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    data class CompassData(
        val azimuth: Float,
        val accuracy: Int,
        val pitch: Float,
        val roll: Float
    )

    val compassDataFlow: Flow<CompassData> = callbackFlow {
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rotationMatrix = FloatArray(9)
        val remappedRotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        var lastAzimuth = 0f
        var lastEmittedAzimuth = -1f
        var lastEmittedTime = 0L
        var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val now = System.currentTimeMillis()
                // Throttle updates to max 60 FPS (~16ms) for liquid-smooth response
                if (now - lastEmittedTime < 16) return

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        processRotationMatrix(rotationMatrix, now)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        if (geomagnetic[0] != 0f) {
                            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                processRotationMatrix(rotationMatrix, now)
                            }
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        if (gravity[0] != 0f) {
                            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                processRotationMatrix(rotationMatrix, now)
                            }
                        }
                    }
                }
            }

            private fun processRotationMatrix(matrix: FloatArray, timestamp: Long) {
                val displayRotation = try {
                    windowManager.defaultDisplay.rotation
                } catch (e: Exception) {
                    Surface.ROTATION_0
                }

                var axisX = SensorManager.AXIS_X
                var axisY = SensorManager.AXIS_Y

                when (displayRotation) {
                    Surface.ROTATION_0 -> {
                        axisX = SensorManager.AXIS_X
                        axisY = SensorManager.AXIS_Y
                    }
                    Surface.ROTATION_90 -> {
                        axisX = SensorManager.AXIS_Y
                        axisY = SensorManager.AXIS_MINUS_X
                    }
                    Surface.ROTATION_180 -> {
                        axisX = SensorManager.AXIS_MINUS_X
                        axisY = SensorManager.AXIS_MINUS_Y
                    }
                    Surface.ROTATION_270 -> {
                        axisX = SensorManager.AXIS_MINUS_Y
                        axisY = SensorManager.AXIS_X
                    }
                }

                if (SensorManager.remapCoordinateSystem(matrix, axisX, axisY, remappedRotationMatrix)) {
                    SensorManager.getOrientation(remappedRotationMatrix, orientation)
                    var targetAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (targetAzimuth < 0) targetAzimuth += 360f

                    val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                    // Shortest circular angle difference
                    var delta = targetAzimuth - lastAzimuth
                    if (delta > 180f) delta -= 360f
                    if (delta < -180f) delta += 360f

                    // Deadband noise filter: Ignore micro-tremors smaller than 0.15 degrees
                    if (abs(delta) < 0.15f && lastEmittedAzimuth >= 0f) {
                        return
                    }

                    // Smooth Exponential Moving Average
                    val alpha = when {
                        abs(delta) > 15f -> 0.35f
                        abs(delta) > 5f -> 0.22f
                        else -> 0.15f
                    }

                    val smoothedAzimuth = (lastAzimuth + alpha * delta + 360f) % 360f
                    lastAzimuth = smoothedAzimuth
                    lastEmittedAzimuth = smoothedAzimuth
                    lastEmittedTime = timestamp

                    trySend(CompassData(smoothedAzimuth, currentAccuracy, pitch, roll))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    currentAccuracy = accuracy
                }
            }
        }

        // TIER 1: Rotation Vector Sensor (Highest Precision Gyro + Mag + Accel)
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        // TIER 2: Geomagnetic Rotation Vector (Low Power Fused Mag + Accel)
        val geoRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        // TIER 3: Dual Accelerometer & Magnetometer
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        when {
            rotationVectorSensor != null -> {
                sensorManager.registerListener(sensorListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
            }
            geoRotationSensor != null -> {
                sensorManager.registerListener(sensorListener, geoRotationSensor, SensorManager.SENSOR_DELAY_GAME)
            }
            accelSensor != null && magSensor != null -> {
                sensorManager.registerListener(sensorListener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(sensorListener, magSensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        awaitClose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}
