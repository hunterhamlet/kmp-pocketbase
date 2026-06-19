plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
}

group = "com.hamon.kmp-pocketbase"
version = "0.1.0-alpha02"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("com.squareup:kotlinpoet:2.1.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("pocketbaseCodegen") {
            id = "com.hamon.kmp-pocketbase.codegen"
            implementationClass = "com.hamon.kmp_pocketbase.codegen.PocketbaseCodegenPlugin"
            displayName = "PocketBase Codegen"
            description = "Generates Kotlin Multiplatform models from a PocketBase schema JSON file."
        }
    }
}

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
}