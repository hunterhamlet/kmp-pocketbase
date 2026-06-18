package com.hamon.kmp_pocketbase.network.pocketbase.filter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilterBuilderTest {
    private fun build(block: FilterBuilder.() -> Unit): String? = FilterBuilder().apply(block).buildString()

    @Test
    fun emptyBuilderReturnsNull() {
        assertNull(build { })
    }

    @Test
    fun singleEqCondition() {
        assertEquals("status = \"active\"", build { "status" eq "active" })
    }

    @Test
    fun singleNeqCondition() {
        assertEquals("status != \"inactive\"", build { "status" neq "inactive" })
    }

    @Test
    fun gtWithNumber() {
        assertEquals("age > 18", build { "age" gt 18 })
    }

    @Test
    fun gteWithNumber() {
        assertEquals("age >= 18", build { "age" gte 18 })
    }

    @Test
    fun ltWithNumber() {
        assertEquals("age < 65", build { "age" lt 65 })
    }

    @Test
    fun lteWithNumber() {
        assertEquals("age <= 65", build { "age" lte 65 })
    }

    @Test
    fun likeProducesTildeOperator() {
        assertEquals("name ~ \"john\"", build { "name" like "john" })
    }

    @Test
    fun notLikeProducesExclamationTilde() {
        assertEquals("name !~ \"john\"", build { "name" notLike "john" })
    }

    @Test
    fun anyEqProducesQuestionMarkEq() {
        assertEquals("tags ?= \"mobile\"", build { "tags" anyEq "mobile" })
    }

    @Test
    fun anyNeqProducesQuestionMarkNeq() {
        assertEquals("tags ?!= \"deleted\"", build { "tags" anyNeq "deleted" })
    }

    @Test
    fun inRangeWithIntRange() {
        assertEquals("(score >= 50 && score <= 100)", build { "score" inRange (50..100) })
    }

    @Test
    fun notInRangeWithIntRange() {
        assertEquals("(score < 50 || score > 100)", build { "score" notInRange (50..100) })
    }

    @Test
    fun inRangeWithStringDates() {
        assertEquals(
            "(created >= \"2024-01-01\" && created <= \"2024-12-31\")",
            build { "created" inRange ("2024-01-01".."2024-12-31") },
        )
    }

    @Test
    fun twoConditionsCombineWithAnd() {
        assertEquals(
            "(age > 18 && status = \"active\")",
            build {
                "age" gt 18
                "status" eq "active"
            },
        )
    }

    @Test
    fun threeConditionsCombineWithNestedAnd() {
        assertEquals(
            "((a = 1 && b = 2) && c = 3)",
            build {
                "a" eq 1
                "b" eq 2
                "c" eq 3
            },
        )
    }

    @Test
    fun orBlockCombinesWithPipes() {
        assertEquals(
            "(role = \"admin\" || role = \"mod\")",
            build {
                or {
                    "role" eq "admin"
                    "role" eq "mod"
                }
            },
        )
    }

    @Test
    fun notBlockNegatesExpression() {
        assertEquals("!(active = false)", build { not { "active" eq false } })
    }

    @Test
    fun mixedTopLevelAndOrBlock() {
        assertEquals(
            "(age > 18 && (role = \"admin\" || role = \"mod\"))",
            build {
                "age" gt 18
                or {
                    "role" eq "admin"
                    "role" eq "mod"
                }
            },
        )
    }

    @Test
    fun allAnyVariantsProduceCorrectSymbols() {
        assertEquals("n ?> 5", build { "n" anyGt 5 })
        assertEquals("n ?>= 5", build { "n" anyGte 5 })
        assertEquals("n ?< 5", build { "n" anyLt 5 })
        assertEquals("n ?<= 5", build { "n" anyLte 5 })
    }
}
