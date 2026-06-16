package com.hamon.kmp_pocketbase.network.pocketbase.sort

import kotlin.test.Test
import kotlin.test.assertEquals

private enum class TestField(
    override val fieldName: String,
) : SortField {
    NAME("name"),
    CREATED("created"),
}

class SortFieldTest {
    @Test
    fun stringAscReturnsSortOrderWithAscDirection() {
        val order = "name".asc()
        assertEquals("name", order.field)
        assertEquals(SortDirection.ASC, order.direction)
    }

    @Test
    fun stringDescReturnsSortOrderWithDescDirection() {
        val order = "created".desc()
        assertEquals("created", order.field)
        assertEquals(SortDirection.DESC, order.direction)
    }

    @Test
    fun sortOrderBuildWithAscProducesPlusPrefix() {
        assertEquals("+name", "name".asc().build())
    }

    @Test
    fun sortOrderBuildWithDescProducesMinusPrefix() {
        assertEquals("-created", "created".desc().build())
    }

    @Test
    fun sortFieldInterfaceAscDelegatesToFieldName() {
        val field =
            object : SortField {
                override val fieldName = "email"
            }
        assertEquals("+email", field.asc().build())
    }

    @Test
    fun sortFieldInterfaceDescDelegatesToFieldName() {
        val field =
            object : SortField {
                override val fieldName = "updated"
            }
        assertEquals("-updated", field.desc().build())
    }

    @Test
    fun enumImplementingSortFieldWorksCorrectly() {
        assertEquals("+name", TestField.NAME.asc().build())
        assertEquals("-created", TestField.CREATED.desc().build())
    }
}
