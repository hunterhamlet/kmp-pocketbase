package com.hamon.kmp_pocketbase.network.pocketbase

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PocketBaseLogTest {
    private fun captureLog(
        level: PocketBaseLogLevel,
        url: String = "https://example.com/api/collections/users/records",
        collection: String = "users",
        token: String? = null,
        requestHeaders: Map<String, String>? = null,
        requestBody: String? = null,
        status: String = "200 OK",
        responseHeaders: Map<String, String>? = null,
        responseBody: String = "{}",
    ): String? {
        var captured: String? = null
        PocketBaseLog(level, PocketBaseLogger { captured = it }).log(
            url = url,
            collection = collection,
            token = token,
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            status = status,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
        )
        return captured
    }

    @Test
    fun isEnabledReturnsFalseForNoneLevel() {
        assertFalse(PocketBaseLog(PocketBaseLogLevel.NONE, PocketBaseLogger.Default).isEnabled())
    }

    @Test
    fun isEnabledReturnsTrueForBasicLevel() {
        assertTrue(PocketBaseLog(PocketBaseLogLevel.BASIC, PocketBaseLogger.Default).isEnabled())
    }

    @Test
    fun isEnabledReturnsTrueForHeadersLevel() {
        assertTrue(PocketBaseLog(PocketBaseLogLevel.HEADERS, PocketBaseLogger.Default).isEnabled())
    }

    @Test
    fun logDoesNotCallLoggerWhenLevelIsNone() {
        assertNull(captureLog(PocketBaseLogLevel.NONE))
    }

    @Test
    fun logOutputContainsUrlAndCollection() {
        val output = captureLog(PocketBaseLogLevel.BASIC)
        assertNotNull(output)
        assertTrue(output.contains("https://example.com"))
        assertTrue(output.contains("users"))
    }

    @Test
    fun logOutputContainsStatusCode() {
        val output = captureLog(PocketBaseLogLevel.BASIC, status = "201 Created")
        assertNotNull(output)
        assertTrue(output!!.contains("201 Created"))
    }

    @Test
    fun logOutputShowsTokenWhenPresent() {
        val output = captureLog(PocketBaseLogLevel.BASIC, token = "tok123")
        assertNotNull(output)
        assertTrue(output!!.contains("Bearer tok123"))
    }

    @Test
    fun logOutputShowsNoneLabelWhenTokenAbsent() {
        val output = captureLog(PocketBaseLogLevel.BASIC, token = null)
        assertNotNull(output)
        assertTrue(output!!.contains("(none)"))
    }

    @Test
    fun logOutputContainsRequestBodyWhenPresent() {
        val output = captureLog(PocketBaseLogLevel.BASIC, requestBody = """{"name":"Alice"}""")
        assertNotNull(output)
        assertTrue(output!!.contains("Alice"))
    }

    @Test
    fun logOutputContainsResponseBody() {
        val output = captureLog(PocketBaseLogLevel.BASIC, responseBody = """{"id":"r1"}""")
        assertNotNull(output)
        assertTrue(output!!.contains("r1"))
    }

    @Test
    fun logOutputContainsRequestHeadersAtHeadersLevel() {
        val output =
            captureLog(
                PocketBaseLogLevel.HEADERS,
                requestHeaders = mapOf("Content-Type" to "application/json"),
            )
        assertNotNull(output)
        assertTrue(output!!.contains("Content-Type"))
    }

    @Test
    fun logOutputContainsResponseHeadersAtHeadersLevel() {
        val output =
            captureLog(
                PocketBaseLogLevel.HEADERS,
                responseHeaders = mapOf("X-Custom" to "header-value"),
            )
        assertNotNull(output)
        assertTrue(output!!.contains("X-Custom"))
    }

    @Test
    fun logOutputExcludesHeadersAtBasicLevel() {
        val output =
            captureLog(
                PocketBaseLogLevel.BASIC,
                requestHeaders = mapOf("Content-Type" to "application/json"),
                responseHeaders = mapOf("X-Custom" to "header-value"),
            )
        assertNotNull(output)
        assertFalse(output!!.contains("Content-Type"))
        assertFalse(output.contains("X-Custom"))
    }

    @Test
    fun logPrettyPrintsValidJsonBody() {
        val output = captureLog(PocketBaseLogLevel.BASIC, responseBody = """{"id":"r1","name":"Alice"}""")
        assertNotNull(output)
        assertTrue(output!!.contains("id"))
    }

    @Test
    fun logFallsBackToRawStringForNonJsonBody() {
        val rawBody = "plain text response"
        val output = captureLog(PocketBaseLogLevel.BASIC, responseBody = rawBody)
        assertNotNull(output)
        assertTrue(output!!.contains(rawBody))
    }
}
