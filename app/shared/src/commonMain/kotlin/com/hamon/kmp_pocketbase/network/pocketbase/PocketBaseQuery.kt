package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.filter.FilterBuilder
import com.hamon.kmp_pocketbase.network.pocketbase.sort.SortBuilder
import com.hamon.kmp_pocketbase.network.pocketbase.sort.SortOrder

data class QueryParams(
    val filter: String? = null,
    val sort: String? = null,
    val expand: String? = null,
    val fields: String? = null,
    val skipTotal: Boolean = false,
)

class QueryBuilder {
    private var filter: String? = null
    private var sort: String? = null
    private var expand: String? = null
    private var fields: String? = null
    private var skipTotal: Boolean = false

    fun filter(block: FilterBuilder.() -> Unit) {
        filter = FilterBuilder().apply(block).buildString()
    }

    fun sort(block: SortBuilder.() -> Unit) {
        sort = SortBuilder().apply(block).buildString()
    }

    fun sort(vararg orders: SortOrder) {
        sort = orders.joinToString(",") { it.build() }.ifEmpty { null }
    }

    fun expand(vararg relations: String) {
        expand = relations.joinToString(",")
    }

    fun fields(vararg fieldNames: String) {
        fields = fieldNames.joinToString(",")
    }

    fun skipTotal(skip: Boolean = true) {
        skipTotal = skip
    }

    internal fun build(): QueryParams = QueryParams(filter, sort, expand, fields, skipTotal)
}

fun pbQuery(block: QueryBuilder.() -> Unit): QueryParams = QueryBuilder().apply(block).build()
