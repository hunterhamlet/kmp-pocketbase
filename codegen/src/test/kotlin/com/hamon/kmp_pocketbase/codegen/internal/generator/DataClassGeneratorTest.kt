package com.hamon.kmp_pocketbase.codegen.internal.generator

import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.CollectionSchema
import com.hamon.kmp_pocketbase.codegen.internal.parser.schema.FieldSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DataClassGeneratorTest {
    private val generator = DataClassGenerator(TypeMapper(), generateParcelize = true)
    private val generatorNoParcelize = DataClassGenerator(TypeMapper(), generateParcelize = false)

    private fun schema(
        name: String,
        type: String = "base",
        fields: List<FieldSchema> = emptyList(),
    ) = CollectionSchema(id = "test_id", name = name, type = type, fields = fields)

    private fun field(
        name: String,
        type: String,
        required: Boolean = false,
        hidden: Boolean = false,
        onlyInt: Boolean = false,
        maxSelect: Int? = null,
        collectionId: String? = null,
    ) = FieldSchema(
        id = "fid",
        name = name,
        type = type,
        required = required,
        hidden = hidden,
        onlyInt = onlyInt,
        maxSelect = maxSelect,
        collectionId = collectionId,
    )

    @Test
    fun `class name is collection name in PascalCase with Record suffix`() {
        val result = generator.generate("com.example", schema("my_profile"))
        assertTrue(result.content.contains("class MyProfileRecord"))
        assertEquals("MyProfileRecord", result.name)
    }

    @Test
    fun `adds data modifier`() {
        val result = generator.generate("com.example", schema("profile"))
        assertTrue(result.content.contains("data class ProfileRecord"))
    }

    @Test
    fun `adds @Serializable annotation`() {
        val result = generator.generate("com.example", schema("profile"))
        assertTrue(result.content.contains("@Serializable"))
    }

    @Test
    fun `adds @PbParcelize and PbParcelable when enabled`() {
        val result = generator.generate("com.example", schema("profile"))
        assertTrue(result.content.contains("@PbParcelize"))
        assertTrue(result.content.contains("PbParcelable"))
    }

    @Test
    fun `omits @PbParcelize and PbParcelable when disabled`() {
        val result = generatorNoParcelize.generate("com.example", schema("profile"))
        assertFalse(result.content.contains("PbParcelize"))
        assertFalse(result.content.contains("PbParcelable"))
    }

    @Test
    fun `converts snake_case field name to camelCase property`() {
        val fields = listOf(field("full_name", "text", required = true))
        val result = generator.generate("com.example", schema("profile", fields = fields))
        assertTrue(result.content.contains("fullName"))
    }

    @Test
    fun `adds @SerialName for snake_case fields`() {
        val fields = listOf(field("full_name", "text", required = true))
        val result = generator.generate("com.example", schema("profile", fields = fields))
        assertTrue(result.content.contains("""@SerialName("full_name")"""))
    }

    @Test
    fun `does not add @SerialName for single-word field names`() {
        val fields = listOf(field("id", "text", required = true))
        val result = generator.generate("com.example", schema("profile", fields = fields))
        assertFalse(result.content.contains("@SerialName"))
    }

    @Test
    fun `optional field has nullable type and null default`() {
        val fields = listOf(field("birthday", "date", required = false))
        val result = generator.generate("com.example", schema("profile", fields = fields))
        assertTrue(result.content.contains("String? = null"))
    }

    @Test
    fun `required field has non-nullable type`() {
        val fields = listOf(field("full_name", "text", required = true))
        val result = generator.generate("com.example", schema("profile", fields = fields))
        assertTrue(result.content.contains("fullName: String"))
        assertFalse(result.content.contains("fullName: String?"))
    }

    @Test
    fun `multi-relation field maps to List with emptyList default`() {
        val fields = listOf(field("machine_id", "relation", maxSelect = 999, collectionId = "abc"))
        val result = generator.generate("com.example", schema("exercise", fields = fields))
        assertTrue(result.content.contains("List<String>"))
        assertTrue(result.content.contains("emptyList()"))
    }

    @Test
    fun `hidden fields are excluded`() {
        val fields = listOf(
            field("token_key", "text", hidden = true),
            field("name", "text", required = true, hidden = false),
        )
        val result = generator.generate("com.example", schema("account", fields = fields))
        assertFalse(result.content.contains("tokenKey"))
        assertTrue(result.content.contains("name"))
    }

    @Test
    fun `password type fields are excluded`() {
        val fields = listOf(
            field("password", "password"),
            field("email", "email", required = true),
        )
        val result = generator.generate("com.example", schema("account", fields = fields))
        assertFalse(result.content.contains("password:"))
        assertTrue(result.content.contains("email"))
    }
}