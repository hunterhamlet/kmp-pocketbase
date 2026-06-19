package com.hamon.kmp_pocketbase.codegen.internal.parser.schema

import kotlinx.serialization.Serializable

@Serializable
internal data class FieldSchema(
    val id: String,
    val name: String,
    val type: String,
    val required: Boolean = false,
    val system: Boolean = false,
    val hidden: Boolean = false,
    val presentable: Boolean = false,
    val primaryKey: Boolean = false,
    val onlyInt: Boolean = false,
    val onCreate: Boolean = false,
    val onUpdate: Boolean = false,
    val maxSelect: Int? = null,
    val minSelect: Int? = null,
    val values: List<String>? = null,
    val collectionId: String? = null,
    val cascadeDelete: Boolean = false,
)