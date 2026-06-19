package com.hamon.kmp_pocketbase.codegen.internal

internal fun String.toPascalCase(): String =
    split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

internal fun String.toCamelCase(): String =
    toPascalCase().replaceFirstChar { it.lowercase() }