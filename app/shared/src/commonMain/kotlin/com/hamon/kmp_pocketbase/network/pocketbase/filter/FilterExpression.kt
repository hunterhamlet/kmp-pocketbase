package com.hamon.kmp_pocketbase.network.pocketbase.filter

sealed class FilterExpression {
    abstract fun build(): String

    data class Comparison(
        val field: String,
        val op: ComparisonOp,
        val value: Any,
    ) : FilterExpression() {
        override fun build() = "$field ${op.symbol} ${value.toFilterValue()}"
    }

    data class And(
        val left: FilterExpression,
        val right: FilterExpression,
    ) : FilterExpression() {
        override fun build() = "(${left.build()} && ${right.build()})"
    }

    data class Or(
        val left: FilterExpression,
        val right: FilterExpression,
    ) : FilterExpression() {
        override fun build() = "(${left.build()} || ${right.build()})"
    }

    data class Not(
        val expr: FilterExpression,
    ) : FilterExpression() {
        override fun build() = "!(${expr.build()})"
    }

    data class Range(
        val field: String,
        val min: Any,
        val max: Any,
    ) : FilterExpression() {
        override fun build() = "($field >= ${min.toFilterValue()} && $field <= ${max.toFilterValue()})"
    }

    data class NotInRange(
        val field: String,
        val min: Any,
        val max: Any,
    ) : FilterExpression() {
        override fun build() = "($field < ${min.toFilterValue()} || $field > ${max.toFilterValue()})"
    }
}

enum class ComparisonOp(
    val symbol: String,
) {
    EQ("="),
    NEQ("!="),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    LIKE("~"),
    NOT_LIKE("!~"),
    ANY_EQ("?="),
    ANY_NEQ("?!="),
    ANY_GT("?>"),
    ANY_GTE("?>="),
    ANY_LT("?<"),
    ANY_LTE("?<="),
}

private fun Any.toFilterValue(): String =
    when (this) {
        is String -> "\"$this\""
        is Boolean -> toString()
        is Number -> toString()
        else -> "\"$this\""
    }
