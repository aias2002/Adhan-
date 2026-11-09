package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.example.ui.components.getGpsLocation
import com.example.ui.components.resolveLocationAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GpsStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            
            if (isGpsEnabled) {
                Log.d("GpsStateReceiver", "GPS enabled, checking if location changed...")
                CoroutineScope(Dispatchers.IO).launch {
                    getGpsLocation(context) { location ->
                        if (location != null) {
                            resolveLocationAddress(context, location) { city, country ->
                                val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
                                val savedLat = appPrefs.getFloat("saved_latitude", 0f).toDouble()
                                val savedLng = appPrefs.getFloat("saved_longitude", 0f).toDouble()

                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    savedLat, savedLng,
                                    location.latitude, location.longitude,
                                    results
                                )

                                if (results[0] > 5000f || (savedLat == 0.0 && savedLng == 0.0)) {
                                    appPrefs.edit()
                                        .putString("saved_city_name", city)
                                        .putString("saved_country_name", country)
                                        .putFloat("saved_latitude", location.latitude.toFloat())
                                        .putFloat("saved_longitude", location.longitude.toFloat())
                                        .commit()

                                    com.example.util.AdhanScheduler.getPrefs(context).edit()
                                        .putString("saved_city_name", city)
                                        .putString("saved_country_name", country)
                                        .putFloat("saved_latitude", location.latitude.toFloat())
                                        .putFloat("saved_longitude", location.longitude.toFloat())
                                        .commit()

                                    Log.d("GpsStateReceiver", "Location automatically updated to current GPS: $city, $country (${location.latitude}, ${location.longitude})")

                                    try {
                                        com.example.util.NextPrayerTracker.syncAll(context)
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
