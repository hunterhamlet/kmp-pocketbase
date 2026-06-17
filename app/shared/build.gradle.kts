import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kover)
    `maven-publish`
}

group = "com.hamon"
version = "0.1.0"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    iosArm64()
    iosSimulatorArm64()

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
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.ksafe)
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
            implementation(libs.ktor.clientJs)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.HttpClientFactoryKt",
                    "*.HttpClientFactory_jvmKt",
                    "*.PocketBase",
                    "*.PocketBase\$*",
                    "*.realtime.SubscribeRequest",
                    "*.realtime.SubscribeRequest\$Companion",
                    "*.storage.EncryptedTokenStorage",
                    "*.storage.EncryptedTokenStorage\$*",
                )
                annotatedBy("*.Generated")
                packages("*.generated.*")
            }
        }
        total {
            verify {
                rule {
                    minBound(70)
                }
            }
        }
    }
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/hunterhamlet/kmp-pocketbase")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications.withType<MavenPublication> {
            artifactId = artifactId.replace("shared", "kmp-pocketbase")
        }
    }
}
