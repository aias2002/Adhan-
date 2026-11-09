package com.example

import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun appUpdateManager_regexExtractsDetailsAndDetectsNewerVersion() {
        val currentBaseline = "2026-10-25 09:32:30"
        
        // Layout A: standard list items
        val sampleHtmlA = """
            <div class="filename">Adhan+.apk</div>
            <ul class="details">
                <li>File size: <span>41.50MB</span></li>
                <li>Uploaded: <span>2026-11-05 14:20:00</span></li>
            </ul>
            <a class="input popsok" href="https://download.mediafire.com/new_direct_link.apk" id="downloadButton">Download</a>
        """.trimIndent()

        // Layout B: nested span tags in list items (responsive layout)
        val sampleHtmlB = """
            <div class="filename">Adhan+.apk</div>
            <li class="details-item">
                <span>File size:</span> <span>42.10MB</span>
            </li>
            <li class="details-item">
                <span>Uploaded:</span> <span>2026-11-06 08:15:30</span>
            </li>
            <a id="downloadButton" href="https://download.mediafire.com/link_b.apk">Download</a>
        """.trimIndent()

        val uploadPatterns = listOf(
            Pattern.compile("Uploaded:?\\s*(?:</span>\\s*)?<span[^>]*>([^<]+)</span>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Uploaded:?\\s*([0-9]{4}-[0-9]{2}-[0-9]{2}(?:\\s+[0-9]{2}:[0-9]{2}:[0-9]{2})?)", Pattern.CASE_INSENSITIVE)
        )

        val sizePatterns = listOf(
            Pattern.compile("File size:?\\s*(?:</span>\\s*)?<span[^>]*>([^<]+)</span>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("File size:?\\s*([0-9.]+\\s*[KMGT]?B)", Pattern.CASE_INSENSITIVE)
        )

        // Test Layout A
        var dateA = ""
        for (p in uploadPatterns) {
            val m = p.matcher(sampleHtmlA)
            if (m.find()) { dateA = m.group(1)?.trim() ?: ""; break }
        }
        assertEquals("2026-11-05 14:20:00", dateA)
        assertTrue(dateA > currentBaseline)

        // Test Layout B (nested <span>Uploaded:</span> <span>...</span>)
        var dateB = ""
        for (p in uploadPatterns) {
            val m = p.matcher(sampleHtmlB)
            if (m.find()) { dateB = m.group(1)?.trim() ?: ""; break }
        }
        assertEquals("2026-11-06 08:15:30", dateB)
        assertTrue(dateB > currentBaseline)

        // Test Size B
        var sizeB = ""
        for (p in sizePatterns) {
            val m = p.matcher(sampleHtmlB)
            if (m.find()) { sizeB = m.group(1)?.trim() ?: ""; break }
        }
        assertEquals("42.10MB", sizeB)
    }
}
