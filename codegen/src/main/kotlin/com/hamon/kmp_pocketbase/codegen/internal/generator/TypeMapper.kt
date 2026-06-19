package com.hamon.kmp_pocketbase.codegen.internal.generator

import com.hamon.kmp_pocketbase.codegen.internal.toPascalCase
import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.FieldSchema

internal class TypeMapper {

    fun map(field: FieldSchema, collectionName: String): MappedType? = when (field.type) {
        "text", "email", "url" -> simple("String", field.required)
        "number" -> if (field.onlyInt) simple("Int", field.required) else simple("Double", field.required)
        "bool" -> simple("Boolean", field.required)
        "date", "autodate" -> simple("String", field.required)
        "select" -> mapSelect(field, collectionName)
        "relation" -> mapRelation(field)
        "file" -> mapFile(field)
        "json" -> MappedType(
            kotlinType = "JsonElement",
            isNullable = !field.required,
            defaultValue = if (!field.required) "null" else null,
            import = "kotlinx.serialization.json.JsonElement",
        )
        "password" -> null
        else -> simple("String", field.required)
    }

    private fun simple(type: String, required: Boolean) = MappedType(
        kotlinType = type,
        isNullable = !required,
        defaultValue = if (!required) "null" else null,
    )

    private fun mapSelect(field: FieldSchema, collectionName: String): MappedType {
        val enumName = "${collectionName.toPascalCase()}${field.name.toPascalCase()}"
        val isSingle = (field.maxSelect ?: 1) == 1
        return if (isSingle) {
            MappedType(
                kotlinType = enumName,
                isNullable = !field.required,
                defaultValue = if (!field.required) "null" else null,
            )
        } else {
            MappedType(
                kotlinType = "List<$enumName>",
                isNullable = false,
                defaultValue = "emptyList()",
            )
        }
    }

    private fun mapRelation(field: FieldSchema): MappedType {
        val isSingle = (field.maxSelect ?: 1) == 1
        return if (isSingle) {
            simple("String", field.required)
        } else {
            MappedType(kotlinType = "List<String>", isNullable = false, defaultValue = "emptyList()")
        }
    }

    private fun mapFile(field: FieldSchema): MappedType {
        val isSingle = (field.maxSelect ?: 1) == 1
        return if (isSingle) {
            simple("String", field.required)
        } else {
            MappedType(kotlinType = "List<String>", isNullable = false, defaultValue = "emptyList()")
        }
    }
}