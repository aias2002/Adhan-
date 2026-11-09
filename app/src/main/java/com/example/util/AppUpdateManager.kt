package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.BuildConfig
import com.example.MainActivity
import com.example.worker.AppUpdateCheckWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Remote version.json manifest structure.
 */
data class AppUpdateManifest(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val mirrorUrl: String = "",
    val changelog: Map<String, String> = emptyMap(),
    val minVersionCode: Long = 1L
)

enum class AppUpdateStatus {
    UPDATE_AVAILABLE,
    UP_TO_DATE,
    NO_INTERNET,
    SERVER_ERROR,
    INVALID_MANIFEST
}

data class AppUpdateResult(
    val status: AppUpdateStatus,
    val manifest: AppUpdateManifest? = null,
    val installedVersionCode: Long = 0L,
    val installedVersionName: String = "",
    val errorMessageAr: String? = null,
    val errorMessageEn: String? = null
) {
    val isUpdateAvailable: Boolean
        get() = status == AppUpdateStatus.UPDATE_AVAILABLE
}

sealed class LinkOpenResult {
    object OpenedInCustomTabs : LinkOpenResult()
    object OpenedInExternalBrowser : LinkOpenResult()
    data class FallbackCopiedToClipboard(val url: String) : LinkOpenResult()
}

/**
 * Robust App Update Manager:
 * - Single Source of Truth: installed versionCode from PackageInfoCompat vs remote versionCode (numeric integer comparison).
 * - Resilient Link Opening: Custom Tabs -> ACTION_VIEW -> Clipboard copy + Share fallback.
 * - Anti-Loop Notification: Sent exactly once per new remote versionCode, auto-dismissed on update installation or package replaced.
 * - Periodic Background Check via WorkManager with network constraint.
 */
object AppUpdateManager {
    private const val TAG = "AppUpdateManager"

    // =========================================================================
    // UPDATE SOURCES CONFIGURATION
    // =========================================================================
    // TODO: Place your hosted version.json URL here (e.g., GitHub raw, Firebase, or your own server)
    const val MANIFEST_URL = "https://raw.githubusercontent.com/aias2002/Adhan-/main/version.json"

    // Default named fallback APK download URL on MediaFire
    const val DEFAULT_APK_URL = "https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file"

    // Preferences & WorkManager constants
    private const val PREFS_NAME = "app_update_prefs_v2"
    private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"
    private const val KEY_LAST_NOTIFIED_VERSION_CODE = "last_notified_version_code"
    private const val KEY_LAST_KNOWN_REMOTE_CODE = "last_known_remote_code"
    private const val KEY_LAST_KNOWN_REMOTE_NAME = "last_known_remote_name"
    private const val KEY_LAST_KNOWN_APK_URL = "last_known_apk_url"
    private const val KEY_LAST_KNOWN_MIRROR_URL = "last_known_mirror_url"
    private const val KEY_LAST_KNOWN_CHANGELOG_AR = "last_known_changelog_ar"
    private const val KEY_LAST_KNOWN_CHANGELOG_EN = "last_known_changelog_en"

    const val UPDATE_CHANNEL_ID = "app_updates_notification_channel_v2"
    const val UPDATE_NOTIFICATION_ID = 9931
    const val PERIODIC_WORK_NAME = "AppUpdateCheckWork"

    const val EXTRA_OPEN_UPDATE_DIALOG = "extra_open_update_dialog"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // -------------------------------------------------------------------------
    // Installed Version Inspection (Single Source of Truth)
    // -------------------------------------------------------------------------
    fun getInstalledVersionCode(context: Context): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(pInfo)
        } catch (_: Exception) {
            BuildConfig.VERSION_CODE.toLong()
        }
    }

    fun getInstalledVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: BuildConfig.VERSION_NAME
        } catch (_: Exception) {
            BuildConfig.VERSION_NAME
        }
    }

    // -------------------------------------------------------------------------
    // Network Connectivity Check
    // -------------------------------------------------------------------------
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(network) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                @Suppress("DEPRECATION")
                val activeInfo = cm.activeNetworkInfo
                @Suppress("DEPRECATION")
                activeInfo != null && activeInfo.isConnected
            }
        } catch (_: Exception) {
            false
        }
    }

    // -------------------------------------------------------------------------
    // Check For Updates (Core Engine)
    // -------------------------------------------------------------------------
    suspend fun checkForUpdate(
        context: Context,
        isManual: Boolean = false,
        manifestUrl: String = MANIFEST_URL
    ): AppUpdateResult = withContext(Dispatchers.IO) {
        val installedCode = getInstalledVersionCode(context)
        val installedName = getInstalledVersionName(context)

        // 1. Connection check
        if (!isNetworkAvailable(context)) {
            Log.w(TAG, "Update check aborted: No internet connection.")
            return@withContext AppUpdateResult(
                status = AppUpdateStatus.NO_INTERNET,
                installedVersionCode = installedCode,
                installedVersionName = installedName,
                errorMessageAr = "لا يوجد اتصال بالإنترنت",
                errorMessageEn = "No internet connection"
            )
        }

        // 2. Fetch manifest with exponential backoff retries (10s connect, 15s read)
        val jsonString = fetchManifestWithRetry(manifestUrl, maxRetries = 2)
        if (jsonString.isNullOrBlank()) {
            Log.w(TAG, "Update check failed: Server unreachable or empty response.")
            return@withContext AppUpdateResult(
                status = AppUpdateStatus.SERVER_ERROR,
                installedVersionCode = installedCode,
                installedVersionName = installedName,
                errorMessageAr = "تعذّر الوصول إلى خادم التحديثات",
                errorMessageEn = "Server unreachable"
            )
        }

        // 3. Parse manifest JSON
        val manifest = parseManifest(jsonString)
        if (manifest == null || manifest.versionCode <= 0L) {
            Log.e(TAG, "Update check failed: Invalid manifest content.")
            return@withContext AppUpdateResult(
                status = AppUpdateStatus.INVALID_MANIFEST,
                installedVersionCode = installedCode,
                installedVersionName = installedName,
                errorMessageAr = "رابط التحديث غير صالح أو تعذر قراءة البيانات",
                errorMessageEn = "Invalid update manifest format"
            )
        }

        // Save last known remote information for settings UI inspection
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
            .putLong(KEY_LAST_KNOWN_REMOTE_CODE, manifest.versionCode)
            .putString(KEY_LAST_KNOWN_REMOTE_NAME, manifest.versionName)
            .putString(KEY_LAST_KNOWN_APK_URL, manifest.apkUrl)
            .putString(KEY_LAST_KNOWN_MIRROR_URL, manifest.mirrorUrl)
            .putString(KEY_LAST_KNOWN_CHANGELOG_AR, manifest.changelog["ar"] ?: "")
            .putString(KEY_LAST_KNOWN_CHANGELOG_EN, manifest.changelog["en"] ?: "")
            .apply()

        // 4. Pure integer comparison: Remote > Installed
        val hasUpdate = manifest.versionCode > installedCode

        if (hasUpdate) {
            Log.i(TAG, "Update available! Remote: ${manifest.versionCode} (${manifest.versionName}) > Installed: $installedCode ($installedName)")
            val lastNotified = prefs.getLong(KEY_LAST_NOTIFIED_VERSION_CODE, 0L)
            // Post notification only once per new remote versionCode unless manual
            if (isManual || manifest.versionCode > lastNotified) {
                val notified = showUpdateNotification(context, manifest)
                if (notified) {
                    prefs.edit().putLong(KEY_LAST_NOTIFIED_VERSION_CODE, manifest.versionCode).apply()
                }
            }

            AppUpdateResult(
                status = AppUpdateStatus.UPDATE_AVAILABLE,
                manifest = manifest,
                installedVersionCode = installedCode,
                installedVersionName = installedName
            )
        } else {
            Log.i(TAG, "App is up to date. Installed: $installedCode >= Remote: ${manifest.versionCode}")
            // Automatic cleanup: cancel any dangling update notification immediately
            cancelUpdateNotification(context)

            AppUpdateResult(
                status = AppUpdateStatus.UP_TO_DATE,
                manifest = manifest,
                installedVersionCode = installedCode,
                installedVersionName = installedName
            )
        }
    }

    private suspend fun fetchManifestWithRetry(url: String, maxRetries: Int = 2): String? {
        var delayMs = 1000L
        for (attempt in 0..maxRetries) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "AdhanPlus/${BuildConfig.VERSION_NAME} Android")
                    .header("Cache-Control", "no-cache")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        return body
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Manifest fetch attempt $attempt failed: ${e.message}")
            }
            if (attempt < maxRetries) {
                delay(delayMs)
                delayMs *= 2
            }
        }
        return null
    }

    private fun parseManifest(jsonString: String): AppUpdateManifest? {
        return try {
            val json = JSONObject(jsonString)
            val vCode = json.optLong("versionCode", 0L)
            val vName = json.optString("versionName", "")
            val rawApkUrl = json.optString("apkUrl", "")
            val apkUrl = if (rawApkUrl.isNotBlank()) rawApkUrl else DEFAULT_APK_URL
            val mirrorUrl = json.optString("mirrorUrl", "")
            val minVCode = json.optLong("minVersionCode", 1L)

            val changelogMap = mutableMapOf<String, String>()
            val chObj = json.optJSONObject("changelog")
            if (chObj != null) {
                val keys = chObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    changelogMap[k] = chObj.optString(k, "")
                }
            }

            AppUpdateManifest(
                versionCode = vCode,
                versionName = vName,
                apkUrl = apkUrl,
                mirrorUrl = mirrorUrl,
                changelog = changelogMap,
                minVersionCode = minVCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse update manifest JSON: ${e.message}")
            null
        }
    }

    // -------------------------------------------------------------------------
    // Notification Lifecycle & Anti-Loop Dismissal
    // -------------------------------------------------------------------------
    fun showUpdateNotification(context: Context, manifest: AppUpdateManifest): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        createNotificationChannel(context, nm)

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled on device")
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "POST_NOTIFICATIONS not granted")
                return false
            }
        }

        // Tap intent: Open MainActivity and show update dialog
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_UPDATE_DIALOG, true)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPendingIntent = PendingIntent.getActivity(context, 8821, contentIntent, flags)

        val appPrefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
        val isAr = (appPrefs.getString("app_language", "ar") ?: "ar") != "en"

        val title = if (isAr) "🚀 تحديث جديد متوفر (v${manifest.versionName})" else "🚀 New Update Available (v${manifest.versionName})"
        val text = if (isAr) "يتوفر إصدار أحدث لتطبيق Adhan+. اضغط للتفاصيل والتحميل." else "A newer version of Adhan+ is available. Tap to view and download."
        val changelogSnippet = manifest.changelog[if (isAr) "ar" else "en"] ?: manifest.changelog["ar"] ?: ""

        val bigText = buildString {
            append(text)
            if (changelogSnippet.isNotBlank()) {
                append("\n\n")
                append(changelogSnippet)
            }
        }

        val builder = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .addAction(
                android.R.drawable.stat_sys_download,
                if (isAr) "عرض التحديث 📥" else "View Update 📥",
                contentPendingIntent
            )

        return try {
            nm.notify(UPDATE_NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Update notification successfully posted (id=$UPDATE_NOTIFICATION_ID, vCode=${manifest.versionCode})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification: ${e.message}")
            false
        }
    }

    fun cancelUpdateNotification(context: Context) {
        try {
            val nm = NotificationManagerCompat.from(context)
            nm.cancel(UPDATE_NOTIFICATION_ID)
            Log.d(TAG, "Cancelled update notification (id=$UPDATE_NOTIFICATION_ID)")
        } catch (_: Exception) {}
    }

    /**
     * Automatic cleanup called on Application.onCreate and whenever Settings screen opens.
     */
    fun performCleanupIfUpToDate(context: Context) {
        try {
            val installedCode = getInstalledVersionCode(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastKnownRemote = prefs.getLong(KEY_LAST_KNOWN_REMOTE_CODE, 0L)
            if (lastKnownRemote > 0L && installedCode >= lastKnownRemote) {
                cancelUpdateNotification(context)
                prefs.edit().remove(KEY_LAST_NOTIFIED_VERSION_CODE).apply()
            }
        } catch (_: Exception) {}
    }

    /**
     * Called when the app receives ACTION_MY_PACKAGE_REPLACED after update installation.
     */
    fun onPackageReplaced(context: Context) {
        Log.i(TAG, "Package replaced broadcast received! Purging old notifications and update flags.")
        cancelUpdateNotification(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_LAST_NOTIFIED_VERSION_CODE)
            .remove(KEY_LAST_KNOWN_REMOTE_CODE)
            .apply()
    }

    /**
     * Called on Application.onCreate.
     */
    fun onAppLaunched(context: Context) {
        performCleanupIfUpToDate(context)
        schedulePeriodicUpdateWorker(context)
    }

    private fun createNotificationChannel(context: Context, nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تحديثات التطبيق"
            val desc = "إشعارات توفر إصدارات وتحديثات جديدة للتطبيق"
            val channel = NotificationChannel(UPDATE_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
                setShowBadge(true)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }

    // -------------------------------------------------------------------------
    // Safe Link Opening Sequence: Custom Tabs -> ACTION_VIEW -> Clipboard Fallback
    // -------------------------------------------------------------------------
    fun openUpdateUrl(context: Context, rawUrl: String): LinkOpenResult {
        val cleanUrl = sanitizeUrl(if (rawUrl.isNotBlank()) rawUrl else DEFAULT_APK_URL)
        val uri = Uri.parse(cleanUrl)

        // 1. Try Chrome Custom Tabs
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(context, uri)
            Log.d(TAG, "Successfully opened URL with Custom Tabs: $cleanUrl")
            return LinkOpenResult.OpenedInCustomTabs
        } catch (e: Exception) {
            Log.w(TAG, "Custom Tabs failed: ${e.message}, attempting standard ACTION_VIEW")
        }

        // 2. Try standard ACTION_VIEW intent
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
            Log.d(TAG, "Successfully opened URL with ACTION_VIEW: $cleanUrl")
            return LinkOpenResult.OpenedInExternalBrowser
        } catch (e: Exception) {
            Log.e(TAG, "ACTION_VIEW failed: ${e.message}, falling back to clipboard copy")
        }

        // 3. Complete fallback: copy to clipboard and return signal to show fallback dialog
        copyToClipboard(context, cleanUrl)
        return LinkOpenResult.FallbackCopiedToClipboard(cleanUrl)
    }

    fun sanitizeUrl(rawUrl: String): String {
        return rawUrl.trim()
            .replace("Adhan+.apk", "Adhan%2B.apk")
    }

    fun copyToClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Download Link", text)
            clipboard?.setPrimaryClip(clip)
        } catch (_: Exception) {}
    }

    fun shareUrl(context: Context, url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, null).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------------------
    // WorkManager Background Check Scheduler
    // -------------------------------------------------------------------------
    fun schedulePeriodicUpdateWorker(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AppUpdateCheckWorker>(
                12, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "Enqueued periodic WorkManager update check (every 12 hours)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule WorkManager update check: ${e.message}")
        }
    }
}
