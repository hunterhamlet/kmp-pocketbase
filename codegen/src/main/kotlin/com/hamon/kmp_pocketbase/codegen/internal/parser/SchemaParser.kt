package com.hamon.kmp_pocketbase.codegen.internal.parser

import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.CollectionSchema
import kotlinx.serialization.json.Json
import java.io.File

internal class SchemaParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(schemaFile: File): List<CollectionSchema> =
        json.decodeFromString(schemaFile.readText())
}