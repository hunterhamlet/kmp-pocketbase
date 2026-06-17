package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.storage.InMemoryTokenStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStoreRestoreTest {
    @Test
    fun restoreLoadsPersistedToken() =
        runTest {
            val storage = InMemoryTokenStorage()
            val store = AuthStore(storage)
            store.save("persisted-token", RecordModel(fields = Unit))

            val restoredStore = AuthStore(storage)
            restoredStore.restore()

            assertEquals("persisted-token", restoredStore.token)
            assertTrue(restoredStore.isValid)
        }

    @Test
    fun restoreWithNoPersistedTokenLeavesStoreInvalid() =
        runTest {
            val store = AuthStore(InMemoryTokenStorage())
            store.restore()
            assertFalse(store.isValid)
            assertNull(store.token)
        }

    @Test
    fun clearAfterRestoreRemovesTokenFromStorage() =
        runTest {
            val storage = InMemoryTokenStorage()
            val store = AuthStore(storage)
            store.save("tok", RecordModel(fields = Unit))
            store.clear()

            val restoredStore = AuthStore(storage)
            restoredStore.restore()

            assertFalse(restoredStore.isValid)
        }
}
