package com.example.util

import android.hardware.GeomagneticField
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaCalculator {
    // Kaaba Coordinates in Mecca
    const val KAABA_LAT = 21.4225241
    const val KAABA_LNG = 39.8261818

    /**
     * Calculates the Qibla bearing relative to True North in degrees (0..360)
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLngRad = Math.toRadians(KAABA_LNG)

        val deltaLng = kaabaLngRad - userLngRad

        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLng)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad).toFloat()

        if (qiblaDeg < 0) {
            qiblaDeg += 360f
        }
        return qiblaDeg
    }

    /**
     * Calculates distance to Mecca in kilometers
     */
    fun calculateDistanceToKaaba(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(KAABA_LAT - userLat)
        val dLng = Math.toRadians(KAABA_LNG - userLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LAT)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    /**
     * Gets geomagnetic declination (difference between magnetic North and true North)
     */
    fun getDeclination(userLat: Double, userLng: Double, altitudeMeters: Double = 0.0): Float {
        val geoField = GeomagneticField(
            userLat.toFloat(),
            userLng.toFloat(),
            altitudeMeters.toFloat(),
            System.currentTimeMillis()
        )
        return geoField.declination
    }
}
