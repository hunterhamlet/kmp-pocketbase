package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.createHttpClient
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeService
import com.hamon.kmp_pocketbase.network.pocketbase.storage.EncryptedTokenStorage
import com.hamon.kmp_pocketbase.network.pocketbase.storage.InMemoryTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PocketBase(
    private val baseUrl: String,
    tokenPersistence: TokenPersistence = TokenPersistence.None,
    logLevel: PocketBaseLogLevel = PocketBaseLogLevel.NONE,
    logger: PocketBaseLogger = PocketBaseLogger.Default,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val authStore: AuthStore =
        AuthStore(
            tokenStorage =
                when (tokenPersistence) {
                    TokenPersistence.None -> InMemoryTokenStorage()
                    TokenPersistence.Encrypted -> EncryptedTokenStorage()
                },
        )

    private val client = createHttpClient()
    private val normalizedBaseUrl = baseUrl.trimEnd('/')
    private val log = PocketBaseLog(logLevel, logger)
    private val realtime = RealtimeService(client, normalizedBaseUrl, authStore)
    private val services = mutableMapOf<String, RecordService>()

    private val restoreJob = scope.launch { authStore.restore() }

    suspend fun awaitReady() {
        restoreJob.join()
    }

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

    companion object
}
