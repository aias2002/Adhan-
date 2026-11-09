package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.ALL_PRESET_CITIES
import com.example.data.model.ALGERIA_CITIES
import com.example.data.model.GLOBAL_CITIES
import com.example.data.model.CityPreset
import com.example.data.model.PrayerOffsets
import com.example.ui.theme.CairoFontFamily

@Composable
fun LocationDialog(
    currentCity: String,
    currentCountry: String,
    currentLat: Double,
    currentLng: Double,
    currentOffsets: PrayerOffsets = PrayerOffsets(),
    appLanguage: String = "ar",
    onDismiss: () -> Unit,
    onSave: (city: String, country: String, lat: Double, lng: Double, offsets: PrayerOffsets) -> Unit
) {
    val isAr = appLanguage != "en"
    val context = LocalContext.current
    var cityInput by remember { mutableStateOf(currentCity) }
    var countryInput by remember { mutableStateOf(currentCountry) }
    var latInput by remember { mutableStateOf(currentLat.toString()) }
    var lngInput by remember { mutableStateOf(currentLng.toString()) }

    var fajrOffsetInput by remember { mutableStateOf(currentOffsets.fajr.toString()) }
    var sunriseOffsetInput by remember { mutableStateOf(currentOffsets.sunrise.toString()) }
    var dhuhrOffsetInput by remember { mutableStateOf(currentOffsets.dhuhr.toString()) }
    var asrOffsetInput by remember { mutableStateOf(currentOffsets.asr.toString()) }
    var maghribOffsetInput by remember { mutableStateOf(currentOffsets.maghrib.toString()) }
    var ishaOffsetInput by remember { mutableStateOf(currentOffsets.isha.toString()) }

    var showOffsetsSection by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }

    fun triggerFastLocationFetch() {
        isLocating = true
        getGpsLocation(context) { loc ->
            isLocating = false
            if (loc != null) {
                latInput = String.format(java.util.Locale.US, "%.4f", loc.latitude)
                lngInput = String.format(java.util.Locale.US, "%.4f", loc.longitude)
                resolveLocationAddress(context, loc) { resolvedCity, resolvedCountry ->
                    cityInput = resolvedCity
                    countryInput = resolvedCountry
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            triggerFastLocationFetch()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(
            LocalLayoutDirection provides (if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr),
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = CairoFontFamily)
        ) {
            ProvideTextStyle(value = TextStyle(fontFamily = CairoFontFamily)) {
                Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 10.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (isAr) "إعدادات الموقع والمواقيت" else "Set Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_close_location_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isAr) "إغلاق" else "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // GPS Location Quick Action
                OutlinedButton(
                    onClick = {
                        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (hasFine || hasCoarse) {
                            triggerFastLocationFetch()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_gps_location"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    enabled = !isLocating
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isLocating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isLocating) (if (isAr) "جاري التحديد..." else "Detecting...") else (if (isAr) "تحديد الموقع تلقائياً" else "Detect Location Automatically"),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                var filterQuery by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text(if (isAr) "بحث عن مدينة..." else "Search for city...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Detect exact match to show interactive alert hint
                val exactMatchPreset = remember(filterQuery) {
                    val query = filterQuery.trim()
                    if (query.isEmpty()) null
                    else ALL_PRESET_CITIES.find {
                        it.nameAr.equals(query, ignoreCase = true) ||
                        it.nameEn.equals(query, ignoreCase = true)
                    }
                }

                AnimatedVisibility(visible = exactMatchPreset != null) {
                    exactMatchPreset?.let { preset ->
                        val cityNameDisplay = if (isAr) preset.nameAr else preset.nameEn
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f))
                                .border(1.5.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(14.dp))
                                .clickable {
                                    cityInput = preset.nameAr
                                    countryInput = preset.countryAr
                                    latInput = String.format(java.util.Locale.US, "%.4f", preset.lat)
                                    lngInput = String.format(java.util.Locale.US, "%.4f", preset.lng)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) "تم العثور على: $cityNameDisplay" else "Found: $cityNameDisplay",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = if (isAr) "اضغط هنا لتعيين الموقع" else "Click here to set the location",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Presets Horizontal List
                val filteredPresets = remember(filterQuery) {
                    if (filterQuery.isBlank()) ALGERIA_CITIES
                    else ALL_PRESET_CITIES.filter {
                        it.nameAr.contains(filterQuery, ignoreCase = true) ||
                        it.nameEn.contains(filterQuery, ignoreCase = true) ||
                        it.countryAr.contains(filterQuery, ignoreCase = true) ||
                        it.code.toString().contains(filterQuery)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPresets) { preset ->
                        val isExactMatch = exactMatchPreset != null && preset.nameAr == exactMatchPreset.nameAr
                        val isSelected = cityInput == preset.nameAr
                        val cityNameDisplay = if (isAr) preset.nameAr else preset.nameEn
                        val label = if (preset.code <= 69) "${String.format("%02d", preset.code)} - $cityNameDisplay" else cityNameDisplay

                        val backgroundColor = when {
                            isExactMatch -> MaterialTheme.colorScheme.tertiaryContainer
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val borderColor = when {
                            isExactMatch -> MaterialTheme.colorScheme.tertiary
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        }
                        val textColor = when {
                            isExactMatch -> MaterialTheme.colorScheme.onTertiaryContainer
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(backgroundColor)
                                .border(
                                    if (isExactMatch) 2.5.dp else 1.dp,
                                    borderColor,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    cityInput = preset.nameAr
                                    countryInput = preset.countryAr
                                    latInput = String.format(java.util.Locale.US, "%.4f", preset.lat)
                                    lngInput = String.format(java.util.Locale.US, "%.4f", preset.lng)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSelected || isExactMatch) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isExactMatch) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || isExactMatch) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                // Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text(if (isAr) "المدينة/البلدية" else "City/Municipality") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = countryInput,
                        onValueChange = { countryInput = it },
                        label = { Text(if (isAr) "الدولة" else "Country") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latInput,
                        onValueChange = { latInput = it },
                        label = { Text(if (isAr) "خط العرض" else "Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = lngInput,
                        onValueChange = { lngInput = it },
                        label = { Text(if (isAr) "خط الطول" else "Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Save Action Button
                Button(
                    onClick = {
                        val query = cityInput.trim()
                        val matchingPreset = if (query.isNotEmpty()) {
                            ALL_PRESET_CITIES.find {
                                it.nameAr.equals(query, ignoreCase = true) ||
                                it.nameEn.equals(query, ignoreCase = true)
                            } ?: ALL_PRESET_CITIES.find {
                                it.nameAr.contains(query, ignoreCase = true) ||
                                it.nameEn.contains(query, ignoreCase = true)
                            }
                        } else null

                        val finalCity = matchingPreset?.nameAr ?: cityInput
                        val finalCountry = matchingPreset?.countryAr ?: countryInput
                        val lat = matchingPreset?.lat ?: (latInput.toDoubleOrNull() ?: currentLat)
                        val lng = matchingPreset?.lng ?: (lngInput.toDoubleOrNull() ?: currentLng)

                        onSave(finalCity, finalCountry, lat, lng, currentOffsets)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_location"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                        Text(
                            text = if (isAr) "حفظ وتحديث المواقيت" else "Save & Apply",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
}
}

fun resolveLocationAddress(context: Context, loc: Location, onResolved: (String, String) -> Unit) {
    // Run Geocoder asynchronously to avoid freezing the main UI thread
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        val isAr = try {
            context.resources.configuration.locales[0]?.language == "ar"
        } catch (_: Exception) {
            java.util.Locale.getDefault().language == "ar"
        }
        val fallbackCity = if (isAr) "موقعي (${String.format(java.util.Locale.US, "%.2f, %.2f", loc.latitude, loc.longitude)})" else "Location (${String.format(java.util.Locale.US, "%.2f, %.2f", loc.latitude, loc.longitude)})"
        val fallbackCountry = if (isAr) "محلي" else "Local"

        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale(if (isAr) "ar" else "en"))
            if (!android.location.Geocoder.isPresent()) {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    onResolved(fallbackCity, fallbackCountry)
                }
                return@launch
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                    val addr = addresses.firstOrNull()
                    val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: fallbackCity
                    val country = addr?.countryName ?: fallbackCountry
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        onResolved(city, country)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                val addr = addresses?.firstOrNull()
                val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: fallbackCity
                val country = addr?.countryName ?: fallbackCountry
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    onResolved(city, country)
                }
            }
        } catch (_: Exception) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                onResolved(fallbackCity, fallbackCountry)
            }
        }
    }
}

/**
 * Fast Multi-Source Fusion Location:
 * Combines GPS, Network Cell/WiFi towers, and Passive providers simultaneously.
 * 1. Instantly inspects cached locations across all enabled providers and delivers the best known location immediately.
 * 2. Simultaneously initiates live updates on all available providers (GPS + Network), delivering faster updates and refining accuracy.
 * 3. Enforces an asynchronous safety timeout so execution never hangs.
 */
fun getGpsLocation(context: Context, onResult: (Location?) -> Unit) {
    try {
        val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        var delivered = false
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

        // 1. Instant last location check via Fused Client
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && !delivered) {
                delivered = true
                onResult(location)
            }
        }.addOnFailureListener {}

        // 2. High-precision active location request via Fused Client (30s timeout for offline cold start)
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 500L
        ).setWaitForAccurateLocation(false)
         .setMaxUpdateAgeMillis(30000L)
         .build()

        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val loc = result.lastLocation
                if (loc != null && !delivered) {
                    delivered = true
                    try { fusedClient.removeLocationUpdates(this) } catch (_: Exception) {}
                    onResult(loc)
                }
            }
        }

        fusedClient.requestLocationUpdates(locationRequest, callback, android.os.Looper.getMainLooper())

        // 3. Safety timeout / fallback to LocationManager multi-source fusion after 30 seconds
        mainHandler.postDelayed({
            if (!delivered) {
                try { fusedClient.removeLocationUpdates(callback) } catch (_: Exception) {}
                getLocationManagerLocation(context) { loc ->
                    if (!delivered) {
                        delivered = true
                        onResult(loc)
                    }
                }
            }
        }, 30000L)

    } catch (e: Exception) {
        getLocationManagerLocation(context, onResult)
    }
}

private fun getLocationManagerLocation(context: Context, onResult: (Location?) -> Unit) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onResult(null)
            return
        }

        val allProviders = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in allProviders) {
            try {
                if (lm.isProviderEnabled(provider)) {
                    val l = lm.getLastKnownLocation(provider)
                    if (l != null) {
                        val isBetter = bestLocation == null ||
                                l.accuracy < bestLocation.accuracy ||
                                (System.currentTimeMillis() - l.time < System.currentTimeMillis() - (bestLocation.time))
                        if (isBetter) {
                            bestLocation = l
                        }
                    }
                }
            } catch (_: SecurityException) {} catch (_: Exception) {}
        }

        var delivered = false
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

        if (bestLocation != null) {
            delivered = true
            onResult(bestLocation)
        }

        val enabledActiveProviders = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { try { lm.isProviderEnabled(it) } catch (_: Exception) { false } }

        if (enabledActiveProviders.isNotEmpty()) {
            val listeners = mutableListOf<android.location.LocationListener>()

            val timeoutRunnable = Runnable {
                listeners.forEach { try { lm.removeUpdates(it) } catch (_: Exception) {} }
                if (!delivered) {
                    delivered = true
                    onResult(bestLocation)
                }
            }

            mainHandler.postDelayed(timeoutRunnable, 3000L)

            for (provider in enabledActiveProviders) {
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val isSignificantlyBetter = bestLocation == null ||
                                location.accuracy < (bestLocation?.accuracy ?: 1000f) ||
                                !delivered

                        if (isSignificantlyBetter) {
                            bestLocation = location
                            delivered = true
                            onResult(location)
                        }

                        if (location.accuracy <= 25f) {
                            mainHandler.removeCallbacks(timeoutRunnable)
                            listeners.forEach { try { lm.removeUpdates(it) } catch (_: Exception) {} }
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                listeners.add(listener)
                try {
                    lm.requestLocationUpdates(provider, 200L, 1f, listener, android.os.Looper.getMainLooper())
                } catch (_: SecurityException) {} catch (_: Exception) {}
            }
        } else if (!delivered) {
            onResult(bestLocation)
        }
    } catch (_: Exception) {
        onResult(null)
    }
}

@Composable
private fun OffsetAdjusterItem(
    title: String,
    valueStr: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentInt = valueStr.toIntOrNull() ?: 0
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onValueChange((currentInt - 1).toString()) },
                modifier = Modifier.size(28.dp)
            ) {
                Text("-", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = if (currentInt > 0) "+$currentInt" else "$currentInt",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = { onValueChange((currentInt + 1).toString()) },
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
