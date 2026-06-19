package com.hamon.kmp_pocketbase.codegen.internal.generator

import com.hamon.kmp_pocketbase.codegen.internal.parser.SchemaParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ModelGeneratorTest {
    private val schemaFile = File(
        ModelGeneratorTest::class.java.classLoader.getResource("test_schema.json")!!.toURI(),
    )
    private val collections = SchemaParser().parse(schemaFile)

    @Test
    fun `excludes system collections by default`() {
        val output = ModelGenerator("com.example").generate(collections)
        val names = output.commonFiles.map { it.name }
        assertTrue(names.none { it.contains("Superusers") })
    }

    @Test
    fun `includes system collections when flag is false`() {
        val output = ModelGenerator("com.example", excludeSystemCollections = false).generate(collections)
        val names = output.commonFiles.map { it.name }
        assertTrue(names.any { it.contains("Superusers") })
    }

    @Test
    fun `generates data class for each non-system collection`() {
        val output = ModelGenerator("com.example").generate(collections)
        val names = output.commonFiles.map { it.name }
        assertTrue(names.contains("AccountRecord"))
        assertTrue(names.contains("ProfileRecord"))
    }

    @Test
    fun `generates enum for select fields`() {
        val output = ModelGenerator("com.example").generate(collections)
        val names = output.commonFiles.map { it.name }
        assertTrue(names.contains("ProfileGender"))
    }

    @Test
    fun `generates parcelize shims when enabled`() {
        val output = ModelGenerator("com.example", generateParcelize = true).generate(collections)
        assertTrue(output.commonFiles.any { it.name == "PocketbaseParcelable" })
        assertTrue(output.androidFiles.any { it.name == "PocketbaseParcelable" })
        assertTrue(output.jvmFiles.any { it.name == "PocketbaseParcelable" })
        assertTrue(output.jsFiles.any { it.name == "PocketbaseParcelable" })
        assertTrue(output.wasmJsFiles.any { it.name == "PocketbaseParcelable" })
        assertTrue(output.nativeFiles.any { it.name == "PocketbaseParcelable" })
    }

    @Test
    fun `skips parcelize shims when disabled`() {
        val output = ModelGenerator("com.example", generateParcelize = false).generate(collections)
        assertTrue(output.androidFiles.isEmpty())
        assertTrue(output.jvmFiles.isEmpty())
    }

    @Test
    fun `generated ProfileRecord contains expected fields`() {
        val output = ModelGenerator("com.example").generate(collections)
        val profileRecord = output.commonFiles.first { it.name == "ProfileRecord" }
        assertTrue(profileRecord.content.contains("fullName"))
        assertTrue(profileRecord.content.contains("fullLastName"))
        assertTrue(profileRecord.content.contains("@SerialName"))
    }

    @Test
    fun `common files count matches expected`() {
        val output = ModelGenerator("com.example").generate(collections)
        // account (auth) + profile (base) = 2 records
        // profile has gender select = 1 enum
        // parcelize shim = 1
        // total = 4
        assertEquals(4, output.commonFiles.size)
    }
}