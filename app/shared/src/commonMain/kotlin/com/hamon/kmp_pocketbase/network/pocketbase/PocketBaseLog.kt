package com.hamon.kmp_pocketbase.network.pocketbase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal val pocketBaseJson = Json { ignoreUnknownKeys = true }

internal class PocketBaseLog(
    val level: PocketBaseLogLevel,
    private val logger: PocketBaseLogger,
) {
    private val prettyJson =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    fun isEnabled(): Boolean = level != PocketBaseLogLevel.NONE

    fun log(
        url: String,
        collection: String,
        token: String?,
        requestHeaders: Map<String, String>?,
        requestBody: String?,
        status: String,
        responseHeaders: Map<String, String>?,
        responseBody: String,
    ) {
        if (!isEnabled()) return

        val line = "─".repeat(62)
        val sb = StringBuilder()

        sb.appendLine("┌─ PocketBase $line")
        sb.appendLine("│ ▶ REQUEST")
        sb.appendLine("│   url        : $url")
        sb.appendLine("│   collection : $collection")
        sb.appendLine("│   token      : ${token?.let { "Bearer $it" } ?: "(none)"}")

        if (level >= PocketBaseLogLevel.HEADERS && requestHeaders != null) {
            sb.appendLine("│")
            sb.appendLine("│   headers:")
            sb.appendLine("│   {")
            requestHeaders.forEach { (key, value) -> sb.appendLine("│       $key : $value") }
            sb.appendLine("│   }")
        }

        if (requestBody != null) {
            sb.appendLine("│")
            sb.appendLine("│   body:")
            requestBody.prettyOrRaw().lines().forEach { sb.appendLine("│   $it") }
        }

        sb.appendLine("│")
        sb.appendLine("├${"─".repeat(63)}")
        sb.appendLine("│ ◀ RESPONSE  $status")

        if (level >= PocketBaseLogLevel.HEADERS && responseHeaders != null) {
            sb.appendLine("│")
            sb.appendLine("│   headers:")
            sb.appendLine("│   {")
            responseHeaders.forEach { (key, value) -> sb.appendLine("│       $key : $value") }
            sb.appendLine("│   }")
        }

        sb.appendLine("│")
        sb.appendLine("│   body:")
        responseBody.prettyOrRaw().lines().forEach { sb.appendLine("│   $it") }
        sb.appendLine("│")
        sb.append("└${"─".repeat(63)}")

        logger.log(sb.toString())
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun String.prettyOrRaw(): String =
        try {
            val element = prettyJson.parseToJsonElement(this)
            prettyJson.encodeToString(JsonElement.serializer(), element)
        } catch (e: Exception) {
            this
        }
}
