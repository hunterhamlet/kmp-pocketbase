import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("com.hamon.kmp-pocketbase.codegen")
}

val localProps =
    Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { load(it) }
    }
val pocketbaseUrl: String =
    localProps.getProperty(
        "pocketbase.url",
        "https://pocketbase-library-demo.pockethost.io",
    )

val generateAppConfig by tasks.registering {
    val outDir = layout.buildDirectory.dir("generated/appconfig/commonMain/kotlin")
    val url = pocketbaseUrl
    outputs.dir(outDir)
    inputs.property("pocketbaseUrl", url)
    doLast {
        val file = outDir.get().file("com/hamon/kmp_pocketbase/demo/AppConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
package com.hamon.kmp_pocketbase.demo

internal object AppConfig {
    const val POCKETBASE_URL = "$url"
}
            """.trimIndent() + "\n",
        )
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.core)
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = "com.hamon.kmp_pocketbase.app.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(
                generateAppConfig.map {
                    layout.buildDirectory.dir("generated/appconfig/commonMain/kotlin")
                },
            )
            dependencies {
                api(projects.core)
                implementation(libs.kotlinx.serializationJson)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

pocketbaseCodegen {
    schemaFile = rootProject.file("pb_schema.json")
    packageName.set("com.hamon.kmp_pocketbase.generated")
    generateParcelize.set(false)
    excludeSystemCollections.set(true)
}
