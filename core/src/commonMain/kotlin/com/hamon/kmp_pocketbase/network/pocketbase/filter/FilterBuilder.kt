package com.hamon.kmp_pocketbase.network.pocketbase.filter

class FilterBuilder {
    private val conditions = mutableListOf<FilterExpression>()

    infix fun String.eq(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.EQ, value))

    infix fun String.neq(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.NEQ, value))

    infix fun String.gt(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.GT, value))

    infix fun String.gte(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.GTE, value))

    infix fun String.lt(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.LT, value))

    infix fun String.lte(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.LTE, value))

    infix fun String.like(value: String) = add(FilterExpression.Comparison(this, ComparisonOp.LIKE, value))

    infix fun String.notLike(value: String) = add(FilterExpression.Comparison(this, ComparisonOp.NOT_LIKE, value))

    infix fun String.anyEq(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.ANY_EQ, value))

    infix fun String.anyNeq(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.ANY_NEQ, value))

    infix fun String.anyGt(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.ANY_GT, value))

    infix fun String.anyGte(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.ANY_GTE, value))

    infix fun String.anyLt(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.ANY_LT, value))

    infix fun String.anyLte(value: Any) = add(FilterExpression.Comparison(this, ComparisonOp.ANY_LTE, value))

    infix fun String.inRange(range: ClosedRange<*>) =
        add(FilterExpression.Range(this, range.start as Any, range.endInclusive as Any))

    infix fun String.notInRange(range: ClosedRange<*>) =
        add(FilterExpression.NotInRange(this, range.start as Any, range.endInclusive as Any))

    fun or(block: FilterBuilder.() -> Unit) {
        val inner = FilterBuilder().apply(block)
        val expr = inner.conditions.reduceOrNull { a, b -> FilterExpression.Or(a, b) } ?: return
        add(expr)
    }

    fun not(block: FilterBuilder.() -> Unit) {
        val inner = FilterBuilder().apply(block)
        val expr = inner.conditions.reduceOrNull { a, b -> FilterExpression.And(a, b) } ?: return
        add(FilterExpression.Not(expr))
    }

    private fun add(expr: FilterExpression) {
        conditions += expr
    }

    internal fun buildString(): String? =
        conditions
            .reduceOrNull { a, b -> FilterExpression.And(a, b) }
            ?.build()
}
