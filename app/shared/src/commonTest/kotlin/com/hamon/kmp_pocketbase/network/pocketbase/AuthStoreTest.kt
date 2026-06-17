package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.storage.InMemoryTokenStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStoreTest {
    private fun authStore() = AuthStore(InMemoryTokenStorage())

    @Test
    fun isValidReturnsFalseWhenTokenIsNull() {
        assertFalse(authStore().isValid)
    }

    @Test
    fun isValidReturnsTrueAfterSave() =
        runTest {
            val store = authStore()
            store.save("tok", RecordModel(fields = Unit))
            assertTrue(store.isValid)
        }

    @Test
    fun modelIsAccessibleAfterSave() =
        runTest {
            val store = authStore()
            val record = RecordModel(id = "abc", fields = Unit)
            store.save("tok", record)
            assertEquals(record, store.model)
        }

    @Test
    fun clearResetsTokenAndModel() =
        runTest {
            val store = authStore()
            store.save("tok", RecordModel(fields = Unit))
            store.clear()
            assertNull(store.token)
            assertNull(store.model)
        }

    @Test
    fun isValidReturnsFalseAfterClear() =
        runTest {
            val store = authStore()
            store.save("tok", RecordModel(fields = Unit))
            store.clear()
            assertFalse(store.isValid)
        }
}
