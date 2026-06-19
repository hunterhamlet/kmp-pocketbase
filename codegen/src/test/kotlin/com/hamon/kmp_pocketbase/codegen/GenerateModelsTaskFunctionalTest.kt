package com.hamon.kmp_pocketbase.codegen

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GenerateModelsTaskFunctionalTest {

    private val minimalSchema = """
        [
          {
            "id": "pbc_001",
            "name": "profile",
            "type": "base",
            "system": false,
            "fields": [
              {
                "id": "f1", "name": "id", "type": "text",
                "required": true, "system": true, "hidden": false,
                "presentable": true, "primaryKey": true
              },
              {
                "id": "f2", "name": "full_name", "type": "text",
                "required": true, "system": false, "hidden": false, "presentable": false
              },
              {
                "id": "f3", "name": "gender", "type": "select",
                "required": false, "system": false, "hidden": false,
                "maxSelect": 1, "values": ["male", "female", "other"]
              }
            ],
            "indexes": []
          }
        ]
    """.trimIndent()

    private fun createProject(): File {
        val dir = createTempDirectory("pb-codegen-functional-test").toFile()

        dir.resolve("settings.gradle.kts").writeText("""rootProject.name = "test-project"""")

        dir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.hamon.kmp-pocketbase.codegen")
            }
            pocketbaseCodegen {
                schemaFile = file("pb_schema.json")
                packageName.set("com.test.models")
                generateParcelize.set(false)
            }
            """.trimIndent(),
        )

        dir.resolve("pb_schema.json").writeText(minimalSchema)
        return dir
    }

    @Test
    fun `task runs successfully`() {
        val result = GradleRunner.create()
            .withProjectDir(createProject())
            .withArguments("generatePocketbaseModels")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generatePocketbaseModels")?.outcome)
    }

    @Test
    fun `generates data class file in commonMain`() {
        val projectDir = createProject()

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generatePocketbaseModels")
            .withPluginClasspath()
            .build()

        val record = projectDir
            .resolve("build/generated/pocketbase/commonMain/kotlin/com/test/models/ProfileRecord.kt")
        assertTrue(record.exists(), "ProfileRecord.kt not found")
        assertTrue(record.readText().contains("data class ProfileRecord"))
    }

    @Test
    fun `generates enum file for select field`() {
        val projectDir = createProject()

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generatePocketbaseModels")
            .withPluginClasspath()
            .build()

        val enum = projectDir
            .resolve("build/generated/pocketbase/commonMain/kotlin/com/test/models/ProfileGender.kt")
        assertTrue(enum.exists(), "ProfileGender.kt not found")
        assertTrue(enum.readText().contains("enum class ProfileGender"))
    }

    @Test
    fun `task is up-to-date on second run`() {
        val projectDir = createProject()
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generatePocketbaseModels")
            .withPluginClasspath()

        runner.build()
        val result = runner.build()

        assertEquals(TaskOutcome.UP_TO_DATE, result.task(":generatePocketbaseModels")?.outcome)
    }
}