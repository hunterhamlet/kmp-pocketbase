package com.hamon.kmp_pocketbase.network.pocketbase.filter

import kotlin.test.Test
import kotlin.test.assertEquals

class FilterExpressionTest {
    @Test
    fun comparisonEqWithStringWrapsInQuotes() {
        val expr = FilterExpression.Comparison("status", ComparisonOp.EQ, "active")
        assertEquals("status = \"active\"", expr.build())
    }

    @Test
    fun comparisonGtWithNumberIsUnquoted() {
        val expr = FilterExpression.Comparison("age", ComparisonOp.GT, 18)
        assertEquals("age > 18", expr.build())
    }

    @Test
    fun comparisonWithBooleanIsUnquoted() {
        val expr = FilterExpression.Comparison("active", ComparisonOp.EQ, true)
        assertEquals("active = true", expr.build())
    }

    @Test
    fun comparisonLikeUsesSymbolTilde() {
        val expr = FilterExpression.Comparison("name", ComparisonOp.LIKE, "john")
        assertEquals("name ~ \"john\"", expr.build())
    }

    @Test
    fun comparisonAnyEqUsesQuestionMarkEq() {
        val expr = FilterExpression.Comparison("tags", ComparisonOp.ANY_EQ, "mobile")
        assertEquals("tags ?= \"mobile\"", expr.build())
    }

    @Test
    fun andWrapsInParenthesesWithDoubleAmpersand() {
        val left = FilterExpression.Comparison("a", ComparisonOp.EQ, 1)
        val right = FilterExpression.Comparison("b", ComparisonOp.EQ, 2)
        assertEquals("(a = 1 && b = 2)", FilterExpression.And(left, right).build())
    }

    @Test
    fun orWrapsInParenthesesWithDoublePipe() {
        val left = FilterExpression.Comparison("role", ComparisonOp.EQ, "admin")
        val right = FilterExpression.Comparison("role", ComparisonOp.EQ, "mod")
        assertEquals("(role = \"admin\" || role = \"mod\")", FilterExpression.Or(left, right).build())
    }

    @Test
    fun notPrefixesWithExclamation() {
        val inner = FilterExpression.Comparison("active", ComparisonOp.EQ, false)
        assertEquals("!(active = false)", FilterExpression.Not(inner).build())
    }

    @Test
    fun rangeProducesGteAndLteExpression() {
        val expr = FilterExpression.Range("score", 50, 100)
        assertEquals("(score >= 50 && score <= 100)", expr.build())
    }

    @Test
    fun notInRangeProducesLtAndGtExpression() {
        val expr = FilterExpression.NotInRange("score", 50, 100)
        assertEquals("(score < 50 || score > 100)", expr.build())
    }

    @Test
    fun nestedAndOrProducesCorrectString() {
        val age = FilterExpression.Comparison("age", ComparisonOp.GT, 18)
        val admin = FilterExpression.Comparison("role", ComparisonOp.EQ, "admin")
        val mod = FilterExpression.Comparison("role", ComparisonOp.EQ, "mod")
        val result = FilterExpression.And(age, FilterExpression.Or(admin, mod)).build()
        assertEquals("(age > 18 && (role = \"admin\" || role = \"mod\"))", result)
    }
}
