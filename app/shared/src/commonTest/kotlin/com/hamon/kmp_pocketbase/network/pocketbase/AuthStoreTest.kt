package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStoreTest {
    @Test
    fun isValidReturnsFalseWhenTokenIsNull() {
        assertFalse(AuthStore().isValid)
    }

    @Test
    fun isValidReturnsTrueAfterSave() {
        val store = AuthStore()
        store.save("tok", RecordModel(fields = Unit))
        assertTrue(store.isValid)
    }

    @Test
    fun modelIsAccessibleAfterSave() {
        val store = AuthStore()
        val record = RecordModel(id = "abc", fields = Unit)
        store.save("tok", record)
        assertEquals(record, store.model)
    }

    @Test
    fun clearResetsTokenAndModel() {
        val store = AuthStore()
        store.save("tok", RecordModel(fields = Unit))
        store.clear()
        assertNull(store.token)
        assertNull(store.model)
    }

    @Test
    fun isValidReturnsFalseAfterClear() {
        val store = AuthStore()
        store.save("tok", RecordModel(fields = Unit))
        store.clear()
        assertFalse(store.isValid)
    }
}
