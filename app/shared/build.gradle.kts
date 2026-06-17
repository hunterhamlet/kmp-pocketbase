import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    jacoco
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
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
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlinx.coroutinesCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.clientMock)
            implementation(libs.kotlinx.coroutinesTest)
        }
        androidMain.dependencies {
            implementation(libs.ktor.clientOkHttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientCio)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.clientJs)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

jacoco {
    toolVersion = "0.8.12"
}

private val excludedClasses =
    listOf(
        "**/AppKt.class",
        "**/ComposableSingletons\$AppKt.class",
        "**/Greeting.class",
        "**/JVMPlatform.class",
        "**/Platform_jvmKt.class",
        "**/HttpClientFactoryKt.class",
        "**/HttpClientFactory_jvmKt.class",
        "**/PocketBase.class",
        "**/PocketBase\$*.class",
        "**/generated/resources/**",
        "**/realtime/SubscribeRequest.class",
        "**/realtime/SubscribeRequest\$Companion.class",
        "**/*\$1.class",
        "**/*\$2.class",
        "**/*\$3.class",
        "**/*\$4.class",
        "**/*\$5.class",
    )

val jacocoExecFile = layout.buildDirectory.file("jacoco/jvmTest.exec")

tasks.register<JacocoReport>("jacocoReport") {
    group = "verification"
    dependsOn("jvmTest")
    executionData.setFrom(jacocoExecFile)
    sourceDirectories.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/jvmMain/kotlin",
        ),
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/kotlin/jvm/main")) {
            exclude(excludedClasses)
        },
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoVerify") {
    group = "verification"
    dependsOn("jacocoReport")
    executionData.setFrom(jacocoExecFile)
    sourceDirectories.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/jvmMain/kotlin",
        ),
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/kotlin/jvm/main")) {
            exclude(excludedClasses)
        },
    )
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
