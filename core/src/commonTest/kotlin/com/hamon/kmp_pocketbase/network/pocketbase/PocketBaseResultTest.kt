package com.hamon.kmp_pocketbase.network.pocketbase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

class PocketBaseResultTest {
    @Test
    fun getOrNullReturnsDataOnSuccess() {
        assertEquals("hello", PocketBaseResult.Success("hello").getOrNull())
    }

    @Test
    fun getOrNullReturnsNullOnFailure() {
        assertNull(PocketBaseResult.Failure(RuntimeException()).getOrNull())
    }

    @Test
    fun exceptionOrNullReturnsExceptionOnFailure() {
        val ex = RuntimeException("oops")
        assertEquals(ex, PocketBaseResult.Failure(ex).exceptionOrNull())
    }

    @Test
    fun exceptionOrNullReturnsNullOnSuccess() {
        assertNull(PocketBaseResult.Success(42).exceptionOrNull())
    }

    @Test
    fun onSuccessIsCalledWithData() {
        var received: String? = null
        PocketBaseResult.Success("data").onSuccess { received = it }
        assertEquals("data", received)
    }

    @Test
    fun onSuccessIsNotCalledOnFailure() {
        var called = false
        PocketBaseResult.Failure(RuntimeException()).onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun onFailureIsCalledWithException() {
        var received: Throwable? = null
        val ex = RuntimeException("error")
        PocketBaseResult.Failure(ex).onFailure { received = it }
        assertEquals(ex, received)
    }

    @Test
    fun onFailureIsNotCalledOnSuccess() {
        var called = false
        PocketBaseResult.Success("ok").onFailure { called = true }
        assertFalse(called)
    }

    @Test
    fun mapTransformsSuccessData() {
        assertEquals(10, PocketBaseResult.Success(5).map { it * 2 }.getOrNull())
    }

    @Test
    fun mapPropagatesFailure() {
        val ex = RuntimeException("fail")
        val result = PocketBaseResult.Failure(ex).map { "ignored" }
        assertIs<PocketBaseResult.Failure>(result)
        assertEquals(ex, result.exceptionOrNull())
    }
}
