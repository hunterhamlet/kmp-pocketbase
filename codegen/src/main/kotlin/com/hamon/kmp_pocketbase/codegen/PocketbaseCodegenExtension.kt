package com.hamon.kmp_pocketbase.codegen

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class PocketbaseCodegenExtension {
    abstract val schemaFile: RegularFileProperty
    abstract val outputDir: DirectoryProperty
    abstract val packageName: Property<String>
    abstract val excludeSystemCollections: Property<Boolean>
    abstract val generateParcelize: Property<Boolean>
}