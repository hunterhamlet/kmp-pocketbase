package com.hamon.kmp_pocketbase.network.pocketbase.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryTokenStorageTest {
    @Test
    fun loadReturnsNullInitially() =
        runTest {
            assertNull(InMemoryTokenStorage().load())
        }

    @Test
    fun loadReturnsSavedToken() =
        runTest {
            val storage = InMemoryTokenStorage()
            storage.save("abc123")
            assertEquals("abc123", storage.load())
        }

    @Test
    fun clearMakesLoadReturnNull() =
        runTest {
            val storage = InMemoryTokenStorage()
            storage.save("abc123")
            storage.clear()
            assertNull(storage.load())
        }

    @Test
    fun saveOverwritesPreviousToken() =
        runTest {
            val storage = InMemoryTokenStorage()
            storage.save("first")
            storage.save("second")
            assertEquals("second", storage.load())
        }
}
