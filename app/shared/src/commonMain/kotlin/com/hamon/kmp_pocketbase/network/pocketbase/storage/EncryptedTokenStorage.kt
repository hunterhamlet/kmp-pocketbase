package com.hamon.kmp_pocketbase.network.pocketbase.storage

internal expect class EncryptedTokenStorage() : TokenStorage {
    override suspend fun save(token: String)
    override suspend fun load(): String?
    override suspend fun clear()
}
