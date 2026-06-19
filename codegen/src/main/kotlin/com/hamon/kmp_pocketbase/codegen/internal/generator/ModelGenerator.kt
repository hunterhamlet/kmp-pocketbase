package com.hamon.kmp_pocketbase.codegen.internal.generator

import com.hamon.kmp_pocketbase.codegen.internal.toPascalCase
import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.CollectionSchema

internal class ModelGenerator(
    private val packageName: String,
    private val excludeSystemCollections: Boolean = true,
    private val generateParcelize: Boolean = true,
) {
    private val typeMapper = TypeMapper()
    private val enumGenerator = EnumGenerator()
    private val dataClassGenerator = DataClassGenerator(typeMapper, generateParcelize)

    fun generate(collections: List<CollectionSchema>): GeneratedOutput {
        val targets = if (excludeSystemCollections) collections.filter { !it.system } else collections

        val commonFiles = mutableListOf<GeneratedFile>()

        targets.forEach { schema ->
            schema.fields
                .filter { !it.hidden && it.type == "select" && !it.values.isNullOrEmpty() }
                .forEach { field ->
                    val enumName = "${schema.name.toPascalCase()}${field.name.toPascalCase()}"
                    commonFiles.add(enumGenerator.generate(packageName, enumName, field.values!!))
                }
            commonFiles.add(dataClassGenerator.generate(packageName, schema))
        }

        val shims = if (generateParcelize) ParcelableShimGenerator.generate(packageName) else emptyMap()

        return GeneratedOutput(
            commonFiles = commonFiles + shims.filesFor(ShimTarget.COMMON),
            androidFiles = shims.filesFor(ShimTarget.ANDROID),
            jvmFiles = shims.filesFor(ShimTarget.JVM),
            jsFiles = shims.filesFor(ShimTarget.JS),
            wasmJsFiles = shims.filesFor(ShimTarget.WASM_JS),
            nativeFiles = shims.filesFor(ShimTarget.NATIVE),
        )
    }

    private fun Map<ShimTarget, GeneratedFile>.filesFor(target: ShimTarget) =
        this[target]?.let { listOf(it) } ?: emptyList()
}