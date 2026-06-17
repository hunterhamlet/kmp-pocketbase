package com.hamon.kmp_pocketbase.network.pocketbase.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultListTest {
    @Test
    fun copyProducesNewInstanceWithChangedPage() {
        val original = ResultList<Unit>(page = 1, totalPages = 3, totalItems = 60)
        val next = original.copy(page = 2)
        assertEquals(2, next.page)
        assertEquals(3, next.totalPages)
    }

    @Test
    fun equalityHoldsForSameValues() {
        val a = ResultList<Unit>(page = 1, perPage = 30, totalItems = 5, totalPages = 1)
        val b = ResultList<Unit>(page = 1, perPage = 30, totalItems = 5, totalPages = 1)
        assertEquals(a, b)
    }

    @Test
    fun defaultConstructorHasEmptyItems() {
        assertTrue(ResultList<Unit>().items.isEmpty())
    }
}
