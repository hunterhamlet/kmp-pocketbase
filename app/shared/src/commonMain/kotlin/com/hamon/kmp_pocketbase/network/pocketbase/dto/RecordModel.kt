package com.hamon.kmp_pocketbase.network.pocketbase.dto

import com.hamon.kmp_pocketbase.network.pocketbase.pocketBaseJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

data class RecordModel<T>(
    val id: String = "",
    val collectionId: String = "",
    val collectionName: String = "",
    val created: String = "",
    val updated: String = "",
    val fields: T,
)

internal fun <T> JsonObject.toRecordModel(serializer: KSerializer<T>): RecordModel<T> =
    RecordModel(
        id = get("id")?.jsonPrimitive?.contentOrNull ?: "",
        collectionId = get("collectionId")?.jsonPrimitive?.contentOrNull ?: "",
        collectionName = get("collectionName")?.jsonPrimitive?.contentOrNull ?: "",
        created = get("created")?.jsonPrimitive?.contentOrNull ?: "",
        updated = get("updated")?.jsonPrimitive?.contentOrNull ?: "",
        fields = pocketBaseJson.decodeFromJsonElement(serializer, this),
    )
