package com.hamon.kmp_pocketbase.codegen

import org.gradle.api.Plugin
import org.gradle.api.Project

class PocketbaseCodegenPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // DSL extension and task registration will be wired in feature/codegen-gradle-task
    }
}