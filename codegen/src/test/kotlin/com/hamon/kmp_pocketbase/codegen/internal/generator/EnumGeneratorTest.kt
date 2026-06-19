package com.hamon.kmp_pocketbase.codegen.internal.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class EnumGeneratorTest {
    private val generator = EnumGenerator()

    @Test
    fun `generated file name matches enum name`() {
        val result = generator.generate("com.example", "ProfileGender", listOf("male"))
        assertEquals("ProfileGender", result.name)
    }

    @Test
    fun `generates enum class declaration`() {
        val result = generator.generate("com.example", "ProfileGender", listOf("male"))
        assertTrue(result.content.contains("enum class ProfileGender"))
    }

    @Test
    fun `adds @Serializable annotation`() {
        val result = generator.generate("com.example", "ProfileGender", listOf("male"))
        assertTrue(result.content.contains("@Serializable"))
    }

    @Test
    fun `generates all enum constants`() {
        val result = generator.generate("com.example", "ProfileGender", listOf("male", "female", "other"))
        assertTrue(result.content.contains("male"))
        assertTrue(result.content.contains("female"))
        assertTrue(result.content.contains("other"))
    }

    @Test
    fun `includes correct package`() {
        val result = generator.generate("com.example.models", "ProfileGender", listOf("male"))
        assertTrue(result.content.contains("package com.example.models"))
    }

    @Test
    fun `includes auto-generated comment`() {
        val result = generator.generate("com.example", "ProfileGender", listOf("male"))
        assertTrue(result.content.contains("AUTO-GENERATED"))
    }
}