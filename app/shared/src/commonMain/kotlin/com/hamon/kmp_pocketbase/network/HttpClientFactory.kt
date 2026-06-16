package com.hamon.kmp_pocketbase.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun httpClientEngine(): HttpClientEngine

internal fun createHttpClient(enableLogging: Boolean = false): HttpClient =
    HttpClient(httpClientEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        if (enableLogging) {
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) = println("[HTTP] $message")
                    }
                level = LogLevel.ALL
            }
        }
    }
