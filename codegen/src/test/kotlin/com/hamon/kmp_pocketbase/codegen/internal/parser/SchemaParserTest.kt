package com.hamon.kmp_pocketbase.codegen.internal.parser

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SchemaParserTest {
    private val parser = SchemaParser()
    private val schemaFile = File(
        SchemaParserTest::class.java.classLoader.getResource("test_schema.json")!!.toURI(),
    )

    @Test
    fun `parses expected number of collections`() {
        assertEquals(3, parser.parse(schemaFile).size)
    }

    @Test
    fun `parses base collection type`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        assertEquals("base", profile.type)
        assertFalse(profile.system)
    }

    @Test
    fun `parses auth collection type`() {
        val account = parser.parse(schemaFile).first { it.name == "account" }
        assertEquals("auth", account.type)
        assertFalse(account.system)
    }

    @Test
    fun `marks system collection correctly`() {
        val superusers = parser.parse(schemaFile).first { it.name == "_superusers" }
        assertTrue(superusers.system)
    }

    @Test
    fun `parses required text field`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        val fullName = profile.fields.first { it.name == "full_name" }
        assertEquals("text", fullName.type)
        assertTrue(fullName.required)
        assertFalse(fullName.system)
    }

    @Test
    fun `parses select field with values and maxSelect`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        val gender = profile.fields.first { it.name == "gender" }
        assertEquals("select", gender.type)
        assertEquals(1, gender.maxSelect)
        assertEquals(listOf("male", "female", "other"), gender.values)
        assertFalse(gender.required)
    }

    @Test
    fun `parses number field with onlyInt flag`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        val weight = profile.fields.first { it.name == "weigth" }
        assertEquals("number", weight.type)
        assertFalse(weight.onlyInt)
        assertFalse(weight.required)
    }

    @Test
    fun `parses relation field with collectionId and maxSelect`() {
        val account = parser.parse(schemaFile).first { it.name == "account" }
        val profileId = account.fields.first { it.name == "profile_id" }
        assertEquals("relation", profileId.type)
        assertEquals(1, profileId.maxSelect)
        assertEquals("pbc_268752357", profileId.collectionId)
        assertFalse(profileId.required)
    }

    @Test
    fun `parses autodate onCreate field`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        val createAt = profile.fields.first { it.name == "create_at" }
        assertEquals("autodate", createAt.type)
        assertTrue(createAt.onCreate)
        assertFalse(createAt.onUpdate)
    }

    @Test
    fun `parses autodate onUpdate field`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        val updateAt = profile.fields.first { it.name == "update_at" }
        assertTrue(updateAt.onCreate)
        assertTrue(updateAt.onUpdate)
    }

    @Test
    fun `parses primary key field`() {
        val profile = parser.parse(schemaFile).first { it.name == "profile" }
        val id = profile.fields.first { it.name == "id" }
        assertTrue(id.primaryKey)
        assertTrue(id.system)
    }

    @Test
    fun `ignores unknown json fields without throwing`() {
        val collections = parser.parse(schemaFile)
        assertTrue(collections.isNotEmpty())
    }
}