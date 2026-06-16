package com.hamon.kmp_pocketbase.network.pocketbase.sort

class SortBuilder {
    private val orders = mutableListOf<SortOrder>()

    fun add(order: SortOrder) {
        orders += order
    }

    operator fun SortOrder.unaryPlus() {
        orders += this
    }

    internal fun buildString(): String? =
        orders
            .map { it.build() }
            .joinToString(",")
            .ifEmpty { null }
}
