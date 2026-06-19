plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
}

group = "com.hamon.kmp-pocketbase"
version = "0.1.0-alpha01"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.squareup:kotlinpoet:2.1.0")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

gradlePlugin {
    plugins {
        create("pocketbaseCodegen") {
            id = "com.hamon.kmp-pocketbase.codegen"
            implementationClass = "com.hamon.kmp_pocketbase.codegen.PocketbaseCodegenPlugin"
        }
    }
}