package com.hamon.kmp_pocketbase.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.BodyProgress
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun httpClientEngine(): HttpClientEngine

internal fun createHttpClient(): HttpClient =
    HttpClient(httpClientEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(SSE)
        install(BodyProgress)
    }
