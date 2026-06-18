package com.hamon.kmp_pocketbase.network.pocketbase.storage

internal class InMemoryTokenStorage : TokenStorage {
    private var token: String? = null

    override suspend fun save(token: String) {
        this.token = token
    }

    override suspend fun load(): String? = token

    override suspend fun clear() {
        token = null
    }
}
