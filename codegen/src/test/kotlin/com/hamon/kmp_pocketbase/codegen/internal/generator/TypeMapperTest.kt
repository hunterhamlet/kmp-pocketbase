package com.hamon.kmp_pocketbase.codegen.internal.generator

import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.FieldSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TypeMapperTest {
    private val mapper = TypeMapper()

    private fun field(
        type: String,
        name: String = "field_name",
        required: Boolean = false,
        onlyInt: Boolean = false,
        maxSelect: Int? = null,
        values: List<String>? = null,
        collectionId: String? = null,
    ) = FieldSchema(
        id = "test_id",
        name = name,
        type = type,
        required = required,
        onlyInt = onlyInt,
        maxSelect = maxSelect,
        values = values,
        collectionId = collectionId,
    )

    @Test
    fun `text required maps to non-nullable String`() {
        val result = mapper.map(field("text", required = true), "profile")
        assertNotNull(result)
        assertEquals("String", result.kotlinType)
        assertFalse(result.isNullable)
        assertNull(result.defaultValue)
    }

    @Test
    fun `text optional maps to nullable String with null default`() {
        val result = mapper.map(field("text", required = false), "profile")
        assertNotNull(result)
        assertEquals("String", result.kotlinType)
        assertTrue(result.isNullable)
        assertEquals("null", result.defaultValue)
    }

    @Test
    fun `email maps to String`() {
        assertEquals("String", mapper.map(field("email", required = true), "profile")?.kotlinType)
    }

    @Test
    fun `url maps to String`() {
        assertEquals("String", mapper.map(field("url", required = true), "profile")?.kotlinType)
    }

    @Test
    fun `number with onlyInt false maps to Double`() {
        val result = mapper.map(field("number", required = true, onlyInt = false), "profile")
        assertNotNull(result)
        assertEquals("Double", result.kotlinType)
        assertFalse(result.isNullable)
    }

    @Test
    fun `number with onlyInt true maps to Int`() {
        val result = mapper.map(field("number", required = true, onlyInt = true), "profile")
        assertNotNull(result)
        assertEquals("Int", result.kotlinType)
    }

    @Test
    fun `number optional maps to nullable Double`() {
        val result = mapper.map(field("number", required = false, onlyInt = false), "profile")
        assertNotNull(result)
        assertTrue(result.isNullable)
        assertEquals("null", result.defaultValue)
    }

    @Test
    fun `bool maps to Boolean`() {
        assertEquals("Boolean", mapper.map(field("bool", required = true), "profile")?.kotlinType)
    }

    @Test
    fun `date maps to String`() {
        assertEquals("String", mapper.map(field("date", required = true), "profile")?.kotlinType)
    }

    @Test
    fun `autodate maps to String`() {
        assertEquals("String", mapper.map(field("autodate", required = true), "profile")?.kotlinType)
    }

    @Test
    fun `select single required maps to PascalCase enum name`() {
        val result = mapper.map(field("select", name = "gender", required = true, maxSelect = 1), "profile")
        assertNotNull(result)
        assertEquals("ProfileGender", result.kotlinType)
        assertFalse(result.isNullable)
        assertNull(result.defaultValue)
    }

    @Test
    fun `select single optional maps to nullable enum`() {
        val result = mapper.map(field("select", name = "gender", required = false, maxSelect = 1), "profile")
        assertNotNull(result)
        assertEquals("ProfileGender", result.kotlinType)
        assertTrue(result.isNullable)
        assertEquals("null", result.defaultValue)
    }

    @Test
    fun `select multi maps to List of enum with emptyList default`() {
        val result = mapper.map(field("select", name = "allergy", required = false, maxSelect = 9), "diet_preferences")
        assertNotNull(result)
        assertEquals("List<DietPreferencesAllergy>", result.kotlinType)
        assertFalse(result.isNullable)
        assertEquals("emptyList()", result.defaultValue)
    }

    @Test
    fun `relation single required maps to non-nullable String`() {
        val result = mapper.map(field("relation", required = true, maxSelect = 1, collectionId = "abc"), "video")
        assertNotNull(result)
        assertEquals("String", result.kotlinType)
        assertFalse(result.isNullable)
    }

    @Test
    fun `relation single optional maps to nullable String`() {
        val result = mapper.map(field("relation", required = false, maxSelect = 1, collectionId = "abc"), "account")
        assertNotNull(result)
        assertEquals("String", result.kotlinType)
        assertTrue(result.isNullable)
    }

    @Test
    fun `relation multi maps to List of String`() {
        val result = mapper.map(field("relation", required = false, maxSelect = 999, collectionId = "abc"), "exercise")
        assertNotNull(result)
        assertEquals("List<String>", result.kotlinType)
        assertFalse(result.isNullable)
        assertEquals("emptyList()", result.defaultValue)
    }

    @Test
    fun `file single maps to String`() {
        assertEquals("String", mapper.map(field("file", required = true, maxSelect = 1), "profile")?.kotlinType)
    }

    @Test
    fun `file multi maps to List of String`() {
        val result = mapper.map(field("file", required = false, maxSelect = 5), "profile")
        assertNotNull(result)
        assertEquals("List<String>", result.kotlinType)
        assertEquals("emptyList()", result.defaultValue)
    }

    @Test
    fun `json maps to JsonElement with import`() {
        val result = mapper.map(field("json", required = false), "exercise")
        assertNotNull(result)
        assertEquals("JsonElement", result.kotlinType)
        assertEquals("kotlinx.serialization.json.JsonElement", result.import)
    }

    @Test
    fun `password maps to null (skipped)`() {
        assertNull(mapper.map(field("password"), "account"))
    }

    @Test
    fun `unknown type falls back to String`() {
        assertEquals("String", mapper.map(field("future_type", required = true), "profile")?.kotlinType)
    }
}