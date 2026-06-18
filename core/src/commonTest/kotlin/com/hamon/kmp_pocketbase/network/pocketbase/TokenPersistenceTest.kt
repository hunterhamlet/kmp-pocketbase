package com.hamon.kmp_pocketbase.network.pocketbase

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class TokenPersistenceTest {
    @Test
    fun noneIsDistinctFromEncrypted() {
        assertNotEquals<TokenPersistence>(TokenPersistence.None, TokenPersistence.Encrypted)
    }

    @Test
    fun noneIsTokenPersistence() {
        assertIs<TokenPersistence>(TokenPersistence.None)
    }

    @Test
    fun encryptedIsTokenPersistence() {
        assertIs<TokenPersistence>(TokenPersistence.Encrypted)
    }
}
