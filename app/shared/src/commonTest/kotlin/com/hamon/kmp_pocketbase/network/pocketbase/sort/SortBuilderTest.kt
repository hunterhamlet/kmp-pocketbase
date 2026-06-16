package com.hamon.kmp_pocketbase.network.pocketbase.sort

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SortBuilderTest {
    private fun build(block: SortBuilder.() -> Unit): String? = SortBuilder().apply(block).buildString()

    @Test
    fun emptyBuilderReturnsNull() {
        assertNull(build { })
    }

    @Test
    fun singleAscField() {
        assertEquals("+name", build { add("name".asc()) })
    }

    @Test
    fun singleDescField() {
        assertEquals("-created", build { add("created".desc()) })
    }

    @Test
    fun multipleFieldsJoinedWithComma() {
        assertEquals(
            "+name,-created",
            build {
                add("name".asc())
                add("created".desc())
            },
        )
    }

    @Test
    fun orderOfFieldsIsPreserved() {
        assertEquals(
            "-created,+name,+email",
            build {
                add("created".desc())
                add("name".asc())
                add("email".asc())
            },
        )
    }

    @Test
    fun unaryPlusOperatorAddsOrder() {
        assertEquals(
            "+name,-created",
            build {
                +"name".asc()
                +"created".desc()
            },
        )
    }
}
