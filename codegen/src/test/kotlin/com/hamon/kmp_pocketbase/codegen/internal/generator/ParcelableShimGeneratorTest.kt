package com.hamon.kmp_pocketbase.codegen.internal.generator

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ParcelableShimGeneratorTest {
    private val packageName = "com.example.models"
    private val shims = ParcelableShimGenerator.generate(packageName)

    @Test
    fun `generates all six targets`() {
        ShimTarget.entries.forEach { target ->
            assertNotNull(shims[target], "Missing shim for $target")
        }
    }

    @Test
    fun `common shim has expect annotation class`() {
        val content = shims[ShimTarget.COMMON]!!.content
        assertTrue(content.contains("expect annotation class PbParcelize"))
    }

    @Test
    fun `common shim has expect interface`() {
        val content = shims[ShimTarget.COMMON]!!.content
        assertTrue(content.contains("expect interface PbParcelable"))
    }

    @Test
    fun `android shim has actual typealias for Parcelize`() {
        val content = shims[ShimTarget.ANDROID]!!.content
        assertTrue(content.contains("actual typealias PbParcelize = kotlinx.parcelize.Parcelize"))
    }

    @Test
    fun `android shim has actual typealias for Parcelable`() {
        val content = shims[ShimTarget.ANDROID]!!.content
        assertTrue(content.contains("actual typealias PbParcelable = android.os.Parcelable"))
    }

    @Test
    fun `non-android shims have actual no-op annotation class`() {
        listOf(ShimTarget.JVM, ShimTarget.JS, ShimTarget.WASM_JS, ShimTarget.NATIVE).forEach { target ->
            val content = shims[target]!!.content
            assertTrue(content.contains("actual annotation class PbParcelize"), "Failed for $target")
            assertTrue(content.contains("actual interface PbParcelable"), "Failed for $target")
        }
    }

    @Test
    fun `all shims include correct package`() {
        shims.values.forEach { file ->
            assertTrue(file.content.contains("package $packageName"), "Missing package in ${file.name}")
        }
    }

    @Test
    fun `all shim files named PocketbaseParcelable`() {
        shims.values.forEach { file ->
            assertTrue(file.name == "PocketbaseParcelable", "Wrong name: ${file.name}")
        }
    }
}