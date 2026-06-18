package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.sort.SortOrder
import com.hamon.kmp_pocketbase.network.pocketbase.sort.asc
import com.hamon.kmp_pocketbase.network.pocketbase.sort.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PocketBaseQueryTest {
    @Test
    fun sortVarargBuildsCorrectSortString() {
        val params = pbQuery { sort("name".asc(), "created".desc()) }
        assertEquals("+name,-created", params.sort)
    }

    @Test
    fun sortVarargWithEmptyInputSetsNullSort() {
        val params = pbQuery { sort(*emptyArray<SortOrder>()) }
        assertNull(params.sort)
    }

    @Test
    fun skipTotalDefaultsTrueWhenCalledWithNoArgs() {
        val params = pbQuery { skipTotal() }
        assertTrue(params.skipTotal)
    }

    @Test
    fun skipTotalFalseWhenExplicitlyPassedFalse() {
        val params = pbQuery { skipTotal(false) }
        assertEquals(false, params.skipTotal)
    }

    @Test
    fun queryParamsCopyProducesNewInstanceWithChangedField() {
        val original = QueryParams(filter = "a=1", sort = "+name")
        val copy = original.copy(sort = "-name")
        assertEquals("a=1", copy.filter)
        assertEquals("-name", copy.sort)
    }

    @Test
    fun queryParamsEqualityHoldsForSameValues() {
        val a = QueryParams(filter = "x=1")
        val b = QueryParams(filter = "x=1")
        assertEquals(a, b)
    }

    @Test
    fun queryParamsDefaultConstructorHasNoFilterOrSort() {
        val params = QueryParams()
        assertNull(params.filter)
        assertNull(params.sort)
    }
}
