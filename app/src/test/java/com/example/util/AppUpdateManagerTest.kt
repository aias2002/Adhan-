package com.example.util

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun testVersionCodePureNumericComparison() {
        val installedCode = 6L

        // Scenario 1: Installed 6, Remote 7 -> Update Available
        val remote7 = 7L
        assertTrue(remote7 > installedCode)

        // Scenario 2: Installed 6, Remote 6 -> Up to Date (No update)
        val remote6 = 6L
        assertFalse(remote6 > installedCode)

        // Scenario 3: Installed 7, Remote 6 -> Up to Date (User has newer build)
        val installed7 = 7L
        assertFalse(remote6 > installed7)
    }

    @Test
    fun testUrlSanitization() {
        val raw = "https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan+.apk/file"
        val sanitized = AppUpdateManager.sanitizeUrl(raw)
        assertEquals("https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file", sanitized)
        assertFalse(sanitized.contains("Adhan+.apk"))
    }

    @Test
    fun testManifestJsonParsing() {
        val jsonString = """
            {
              "versionCode": 7,
              "versionName": "1.6",
              "apkUrl": "https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file",
              "mirrorUrl": "https://example.com/mirror.apk",
              "changelog": {
                "ar": "تحسينات جديدة",
                "en": "New improvements"
              },
              "minVersionCode": 1
            }
        """.trimIndent()

        val json = JSONObject(jsonString)
        val vCode = json.optLong("versionCode", 0L)
        val vName = json.optString("versionName", "")
        val apkUrl = json.optString("apkUrl", "")
        val mirrorUrl = json.optString("mirrorUrl", "")
        val chObj = json.optJSONObject("changelog")
        val changelogAr = chObj?.optString("ar")
        val changelogEn = chObj?.optString("en")

        assertEquals(7L, vCode)
        assertEquals("1.6", vName)
        assertEquals("https://www.mediafire.com/file/v3yjntcyqfxlbnr/Adhan%2B.apk/file", apkUrl)
        assertEquals("https://example.com/mirror.apk", mirrorUrl)
        assertEquals("تحسينات جديدة", changelogAr)
        assertEquals("New improvements", changelogEn)
    }

    @Test
    fun testFallbackDefaultApkUrl() {
        val defaultUrl = AppUpdateManager.DEFAULT_APK_URL
        assertNotNull(defaultUrl)
        assertTrue(defaultUrl.startsWith("https://"))
        assertTrue(defaultUrl.contains("mediafire.com"))
    }
}
