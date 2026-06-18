package com.hamon.kmp_pocketbase.network.pocketbase.storage

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseAndroidContext
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseNotInitializedException
import eu.anifantakis.lib.ksafe.KSafe

internal actual class EncryptedTokenStorage actual constructor() : TokenStorage {
    private val ksafe: KSafe by lazy {
        val ctx =
            PocketBaseAndroidContext.appContext
                ?: throw PocketBaseNotInitializedException()
        KSafe(ctx)
    }

    actual override suspend fun save(token: String) {
        ksafe.put(TOKEN_KEY, token)
    }

    actual override suspend fun load(): String? {
        val value: String = ksafe.get(TOKEN_KEY, "")
        return value.ifEmpty { null }
    }

    actual override suspend fun clear() {
        ksafe.delete(TOKEN_KEY)
    }

    private companion object {
        const val TOKEN_KEY = "pb_auth_token"
    }
}
