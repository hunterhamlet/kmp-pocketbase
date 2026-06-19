package com.hamon.kmp_pocketbase.codegen.internal.generator

import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.CollectionSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CollectionNamesGeneratorTest {
    private val generator = CollectionNamesGenerator()

    private fun schema(name: String, system: Boolean = false) =
        CollectionSchema(id = "id_$name", name = name, type = "base", system = system)

    @Test
    fun `generated file is named PocketbaseCollection`() {
        val result = generator.generate("com.example", listOf(schema("users")))
        assertEquals("PocketbaseCollection", result.name)
    }

    @Test
    fun `generates object declaration`() {
        val result = generator.generate("com.example", listOf(schema("users")))
        assertTrue(result.content.contains("object PocketbaseCollection"))
    }

    @Test
    fun `generates const val for each collection`() {
        val result = generator.generate("com.example", listOf(schema("users"), schema("posts")))
        assertTrue(result.content.contains("val users"))
        assertTrue(result.content.contains(""""users""""))
        assertTrue(result.content.contains("val posts"))
        assertTrue(result.content.contains(""""posts""""))
    }

    @Test
    fun `includes correct package`() {
        val result = generator.generate("com.example.models", listOf(schema("users")))
        assertTrue(result.content.contains("package com.example.models"))
    }

    @Test
    fun `includes auto-generated comment`() {
        val result = generator.generate("com.example", listOf(schema("users")))
        assertTrue(result.content.contains("AUTO-GENERATED"))
    }

    @Test
    fun `generates empty object for empty collection list`() {
        val result = generator.generate("com.example", emptyList())
        assertTrue(result.content.contains("object PocketbaseCollection"))
    }
}