package com.hamon.kmp_pocketbase.network.pocketbase

sealed class TokenPersistence {
    data object None : TokenPersistence()

    data object Encrypted : TokenPersistence()
}
