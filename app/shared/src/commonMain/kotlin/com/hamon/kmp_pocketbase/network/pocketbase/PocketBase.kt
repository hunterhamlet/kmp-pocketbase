package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.createHttpClient

class PocketBase(
    private val baseUrl: String,
    enableLogging: Boolean = false,
) {
    val authStore: AuthStore = AuthStore()

    private val client = createHttpClient(enableLogging)
    private val normalizedBaseUrl = baseUrl.trimEnd('/')
    private val services = mutableMapOf<String, RecordService>()

    fun collection(name: String): RecordService =
        services.getOrPut(name) {
            RecordService(
                client = client,
                baseUrl = normalizedBaseUrl,
                collectionName = name,
                authStore = authStore,
            )
        }
}
