package com.hamon.kmp_pocketbase.codegen

import com.hamon.kmp_pocketbase.codegen.internal.generator.GeneratedFile
import com.hamon.kmp_pocketbase.codegen.internal.generator.ModelGenerator
import com.hamon.kmp_pocketbase.codegen.internal.parser.SchemaParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateModelsTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schemaFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val excludeSystemCollections: Property<Boolean>

    @get:Input
    abstract val generateParcelize: Property<Boolean>

    init {
        group = "pocketbase"
        description = "Generates Kotlin models from a PocketBase schema JSON file."
    }

    @TaskAction
    fun generate() {
        val collections = SchemaParser().parse(schemaFile.get().asFile)
        val output = ModelGenerator(
            packageName = packageName.get(),
            excludeSystemCollections = excludeSystemCollections.get(),
            generateParcelize = generateParcelize.get(),
        ).generate(collections)

        val base = outputDir.get().asFile
        val pkgPath = packageName.get().replace('.', '/')

        writeFiles(output.commonFiles, base, "commonMain", pkgPath)
        writeFiles(output.androidFiles, base, "androidMain", pkgPath)
        writeFiles(output.jvmFiles, base, "jvmMain", pkgPath)
        writeFiles(output.jsFiles, base, "jsMain", pkgPath)
        writeFiles(output.wasmJsFiles, base, "wasmJsMain", pkgPath)
        writeFiles(output.nativeFiles, base, "nativeMain", pkgPath)
    }

    private fun writeFiles(files: List<GeneratedFile>, base: File, sourceSet: String, pkgPath: String) {
        if (files.isEmpty()) return
        val dir = base.resolve("$sourceSet/kotlin/$pkgPath").also { it.mkdirs() }
        files.forEach { dir.resolve("${it.name}.kt").writeText(it.content) }
    }
}