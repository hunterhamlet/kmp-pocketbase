package com.hamon.kmp_pocketbase.network.pocketbase.sort

interface SortField {
    val fieldName: String

    fun asc(): SortOrder = SortOrder(fieldName, SortDirection.ASC)

    fun desc(): SortOrder = SortOrder(fieldName, SortDirection.DESC)
}

enum class SortDirection(
    val prefix: String,
) {
    ASC("+"),
    DESC("-"),
}

data class SortOrder(
    val field: String,
    val direction: SortDirection,
) {
    fun build(): String = "${direction.prefix}$field"
}

fun String.asc(): SortOrder = SortOrder(this, SortDirection.ASC)

fun String.desc(): SortOrder = SortOrder(this, SortDirection.DESC)
