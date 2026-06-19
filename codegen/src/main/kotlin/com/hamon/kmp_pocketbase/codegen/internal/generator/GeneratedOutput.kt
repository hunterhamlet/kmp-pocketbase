package com.hamon.kmp_pocketbase.codegen.internal.generator

internal data class GeneratedOutput(
    val commonFiles: List<GeneratedFile>,
    val androidFiles: List<GeneratedFile>,
    val jvmFiles: List<GeneratedFile>,
    val jsFiles: List<GeneratedFile>,
    val wasmJsFiles: List<GeneratedFile>,
    val nativeFiles: List<GeneratedFile>,
)