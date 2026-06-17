package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.createHttpClient
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeService

class PocketBase(
    private val baseUrl: String,
    logLevel: PocketBaseLogLevel = PocketBaseLogLevel.NONE,
    logger: PocketBaseLogger = PocketBaseLogger.Default,
) {
    val authStore: AuthStore = AuthStore()

    private val client = createHttpClient()
    private val normalizedBaseUrl = baseUrl.trimEnd('/')
    private val log = PocketBaseLog(logLevel, logger)
    private val realtime = RealtimeService(client, normalizedBaseUrl, authStore)
    private val services = mutableMapOf<String, RecordService>()

    fun collection(name: String): RecordService =
        services.getOrPut(name) {
            RecordService(
                client = client,
                baseUrl = normalizedBaseUrl,
                collectionName = name,
                authStore = authStore,
                log = log,
                realtime = realtime,
            )
        }
}
