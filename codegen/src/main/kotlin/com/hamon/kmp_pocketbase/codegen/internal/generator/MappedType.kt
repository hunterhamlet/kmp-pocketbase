package com.hamon.kmp_pocketbase.codegen.internal.generator

internal data class MappedType(
    val kotlinType: String,
    val isNullable: Boolean,
    val defaultValue: String?,
    val import: String? = null,
)