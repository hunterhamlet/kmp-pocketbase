package com.hamon.kmp_pocketbase.network.pocketbase

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class PocketBaseNotInitializedExceptionTest {
    @Test
    fun isErrorNotException() {
        assertIs<Error>(PocketBaseNotInitializedException())
    }

    @Test
    fun messageContainsInitInstructions() {
        val ex = PocketBaseNotInitializedException()
        assertContains(ex.message ?: "", "PocketBase.init(context)")
    }

    @Test
    fun messageContainsAndroidContext() {
        val ex = PocketBaseNotInitializedException()
        assertContains(ex.message ?: "", "Application.onCreate()")
    }
}
