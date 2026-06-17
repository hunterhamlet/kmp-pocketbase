package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.storage.TokenStorage

class AuthStore internal constructor(
    private val tokenStorage: TokenStorage,
) {
    var token: String? = null
        private set
    var model: RecordModel<*>? = null
        private set

    val isValid: Boolean get() = !token.isNullOrEmpty()

    internal suspend fun save(
        token: String,
        model: RecordModel<*>,
    ) {
        this.token = token
        this.model = model
        tokenStorage.save(token)
    }

    internal suspend fun restore() {
        token = tokenStorage.load()
    }

    suspend fun clear() {
        token = null
        model = null
        tokenStorage.clear()
    }
}
