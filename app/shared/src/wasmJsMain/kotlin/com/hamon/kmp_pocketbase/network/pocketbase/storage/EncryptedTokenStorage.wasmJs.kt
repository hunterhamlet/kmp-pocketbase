package com.hamon.kmp_pocketbase.network.pocketbase.storage

import eu.anifantakis.lib.ksafe.KSafe

internal actual class EncryptedTokenStorage actual constructor() : TokenStorage {
    private val ksafe = KSafe()

    override suspend fun save(token: String) {
        ksafe.put(TOKEN_KEY, token)
    }

    override suspend fun load(): String? {
        val value: String = ksafe.get(TOKEN_KEY, "")
        return value.ifEmpty { null }
    }

    override suspend fun clear() {
        ksafe.delete(TOKEN_KEY)
    }

    private companion object {
        const val TOKEN_KEY = "pb_auth_token"
    }
}
