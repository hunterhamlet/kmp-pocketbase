package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

class AuthStore {
    var token: String? = null
        private set
    var model: RecordModel? = null
        private set

    val isValid: Boolean get() = !token.isNullOrEmpty()

    internal fun save(
        token: String,
        model: RecordModel,
    ) {
        this.token = token
        this.model = model
    }

    fun clear() {
        token = null
        model = null
    }
}
