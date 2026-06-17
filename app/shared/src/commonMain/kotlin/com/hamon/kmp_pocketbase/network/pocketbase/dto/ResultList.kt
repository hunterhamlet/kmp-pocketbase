package com.hamon.kmp_pocketbase.network.pocketbase.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ResultList<T>(
    val page: Int = 1,
    val perPage: Int = 30,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<RecordModel<T>> = emptyList(),
)

internal fun <T> JsonObject.toResultList(serializer: KSerializer<T>): ResultList<T> =
    ResultList(
        page = get("page")?.jsonPrimitive?.intOrNull ?: 1,
        perPage = get("perPage")?.jsonPrimitive?.intOrNull ?: 30,
        totalItems = get("totalItems")?.jsonPrimitive?.intOrNull ?: 0,
        totalPages = get("totalPages")?.jsonPrimitive?.intOrNull ?: 1,
        items = get("items")?.jsonArray?.map { it.jsonObject.toRecordModel(serializer) } ?: emptyList(),
    )
