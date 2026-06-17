import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
}

val detektCompose = libs.detekt.compose
val ktlintCompose = libs.ktlint.compose

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        "detektPlugins"(detektCompose)
        "ktlintRuleset"(ktlintCompose)
    }

    configure<DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        ignoredBuildTypes = listOf("release")
    }

    configure<KtlintExtension> {
        version.set("1.5.0")
        filter {
            exclude("**/generated/**")
            include("**/*.kt", "**/*.kts")
        }
    }

    // Wire detekt into the standard `check` lifecycle.
    // ktlint-gradle already does this for ktlintCheck automatically.
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn("detekt")
    }

    // Auto-install git hooks for every developer on their first build.
    tasks.matching { it.name == "build" }.configureEach {
        dependsOn(rootProject.tasks.named("installGitHooks"))
    }
}

// Evaluated at configuration time: compatible with configuration cache
val gitHooksDirExists = file(".git/hooks").isDirectory

tasks.register<Copy>("installGitHooks") {
    description = "Copia los hooks de config/git-hooks/ a .git/hooks/ con permisos de ejecución."
    enabled = gitHooksDirExists
    from(file("config/git-hooks/"))
    into(file(".git/hooks/"))
    filePermissions { unix("rwxr-xr-x") }
}