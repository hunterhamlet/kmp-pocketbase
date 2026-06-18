package com.hamon.kmp_pocketbase.network.pocketbase.storage

internal interface TokenStorage {
    suspend fun save(token: String)

    suspend fun load(): String?

    suspend fun clear()
}
