package com.hamon.kmp_pocketbase.codegen.internal.parser.schema

import kotlinx.serialization.Serializable

@Serializable
internal data class CollectionSchema(
    val id: String,
    val name: String,
    val type: String,
    val system: Boolean = false,
    val fields: List<FieldSchema> = emptyList(),
    val indexes: List<String> = emptyList(),
    val listRule: String? = null,
    val viewRule: String? = null,
    val createRule: String? = null,
    val updateRule: String? = null,
    val deleteRule: String? = null,
)