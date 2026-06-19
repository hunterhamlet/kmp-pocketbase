package com.hamon.kmp_pocketbase.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class PocketbaseCodegenPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("pocketbaseCodegen", PocketbaseCodegenExtension::class.java)

        extension.outputDir.convention(target.layout.buildDirectory.dir("generated/pocketbase"))
        extension.excludeSystemCollections.convention(true)
        extension.generateParcelize.convention(true)

        val generateTask = target.tasks.register("generatePocketbaseModels", GenerateModelsTask::class.java) { task ->
            task.schemaFile.set(extension.schemaFile)
            task.outputDir.set(extension.outputDir)
            task.packageName.set(extension.packageName)
            task.excludeSystemCollections.set(extension.excludeSystemCollections)
            task.generateParcelize.set(extension.generateParcelize)
        }

        target.tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }
            .configureEach { it.dependsOn(generateTask) }

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmp = target.extensions.getByType(KotlinMultiplatformExtension::class.java)
            mapOf(
                "commonMain" to "commonMain/kotlin",
                "androidMain" to "androidMain/kotlin",
                "jvmMain" to "jvmMain/kotlin",
                "jsMain" to "jsMain/kotlin",
                "wasmJsMain" to "wasmJsMain/kotlin",
                "nativeMain" to "nativeMain/kotlin",
            ).forEach { (sourceSetName, subPath) ->
                kmp.sourceSets.findByName(sourceSetName)?.kotlin?.srcDir(
                    extension.outputDir.map { it.dir(subPath) },
                )
            }
        }
    }
}