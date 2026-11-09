package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ui.components.getGpsLocation
import com.example.ui.components.resolveLocationAddress
import com.example.util.AdhanScheduler
import com.example.widget.CompactPrayerWidgetProvider
import com.example.widget.NextPrayerWidgetProvider
import com.example.widget.PrayerWidgetProvider
import com.example.widget.VerticalPrayerWidgetProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val isGpsEnabled = lm?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
                    lm?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
            if (isGpsEnabled) {
                val loc = suspendCancellableCoroutine { continuation ->
                    getGpsLocation(context) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                }
                if (loc != null) {
                    val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
                    val schedulerPrefs = AdhanScheduler.getPrefs(context)
                    val savedLat = appPrefs.getFloat("saved_latitude", schedulerPrefs.getFloat("saved_latitude", 36.7538f)).toDouble()
                    val savedLng = appPrefs.getFloat("saved_longitude", schedulerPrefs.getFloat("saved_longitude", 3.0588f)).toDouble()

                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        savedLat, savedLng,
                        loc.latitude, loc.longitude,
                        results
                    )
                    val distanceInMeters = results[0]
                    if (distanceInMeters > 300.0f) {
                        val (city, country) = suspendCancellableCoroutine { continuation ->
                            resolveLocationAddress(context, loc) { resolvedCity, resolvedCountry ->
                                if (continuation.isActive) {
                                    continuation.resume(Pair(resolvedCity, resolvedCountry))
                                }
                            }
                        }

                        appPrefs.edit()
                            .putString("saved_city_name", city)
                            .putString("saved_country_name", country)
                            .putFloat("saved_latitude", loc.latitude.toFloat())
                            .putFloat("saved_longitude", loc.longitude.toFloat())
                            .commit()

                        schedulerPrefs.edit()
                            .putString("saved_city_name", city)
                            .putString("saved_country_name", country)
                            .putFloat("saved_latitude", loc.latitude.toFloat())
                            .putFloat("saved_longitude", loc.longitude.toFloat())
                            .commit()

                        val calcMethodId = appPrefs.getInt("calculation_method_id", schedulerPrefs.getInt("calculation_method_id", 16))
                        val asrMethod = appPrefs.getString("asr_method", null)
                            ?: schedulerPrefs.getString("asr_method", "standard")
                            ?: "standard"

                        val newTimings = com.example.data.calculator.AdhanCalculator.calculatePrayerTimes(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            date = java.util.Date(),
                            methodId = calcMethodId,
                            asrMethod = asrMethod
                        )
                        AdhanScheduler.savePrayerTimings(context, newTimings)
                        AdhanScheduler.scheduleAllPrayers(context, newTimings)
                    }
                }
            }

            com.example.util.NextPrayerTracker.syncAll(context)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
