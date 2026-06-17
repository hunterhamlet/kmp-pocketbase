package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeService
import com.hamon.kmp_pocketbase.network.pocketbase.storage.InMemoryTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.BodyProgress
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@Serializable
private data class UploadRecord(val dummy: String? = null)

class FileUploadTest {
    private val baseUrl = "https://test.pocketbase.io"
    private val collectionName = "documents"
    private val recordJson =
        """{"id":"r1","collectionId":"col1","collectionName":"documents","created":"2024-01-01","updated":"2024-01-01"}"""

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun service(
        engine: MockEngine,
        authStore: AuthStore = AuthStore(InMemoryTokenStorage()),
    ): RecordService {
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                install(SSE)
                install(BodyProgress)
            }
        return RecordService(
            client = client,
            baseUrl = baseUrl,
            collectionName = collectionName,
            authStore = authStore,
            log = PocketBaseLog(PocketBaseLogLevel.NONE, PocketBaseLogger.Default),
            realtime = RealtimeService(client, baseUrl, authStore),
        )
    }

    private fun sampleFile() =
        FileUpload(
            field = "attachment",
            filename = "report.pdf",
            data = "PDF content".encodeToByteArray(),
            mimeType = "application/pdf",
        )

    // createWithFiles

    @Test
    fun createWithFilesSendsMultipartRequest() =
        runTest {
            var contentType: String? = null
            val engine =
                MockEngine { request ->
                    contentType = request.body.contentType?.toString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertNotNull(contentType)
            assertTrue(contentType!!.startsWith("multipart/form-data"))
        }

    @Test
    fun createWithFilesUsesPostMethod() =
        runTest {
            var method: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    method = request.method
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertEquals(HttpMethod.Post, method)
        }

    @Test
    fun createWithFilesSendsJsonPayloadField() =
        runTest {
            var bodyText: String? = null
            val engine =
                MockEngine { request ->
                    bodyText = request.body.toByteArray().decodeToString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            val body = buildJsonObject { put("title", "doc") }
            service(engine).createWithFiles<UploadRecord>(body, listOf(sampleFile()))
            assertNotNull(bodyText)
            assertContains(bodyText!!, "@jsonPayload")
            assertContains(bodyText!!, "title")
        }

    @Test
    fun createWithFilesIncludesFilenameInBody() =
        runTest {
            var bodyText: String? = null
            val engine =
                MockEngine { request ->
                    bodyText = request.body.toByteArray().decodeToString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertContains(bodyText!!, "report.pdf")
        }

    @Test
    fun createWithFilesIncludesFieldNameInBody() =
        runTest {
            var bodyText: String? = null
            val engine =
                MockEngine { request ->
                    bodyText = request.body.toByteArray().decodeToString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertContains(bodyText!!, "attachment")
        }

    @Test
    fun createWithFilesSendsAuthHeaderWhenTokenPresent() =
        runTest {
            var authHeader: String? = null
            val engine =
                MockEngine { request ->
                    authHeader = request.headers[HttpHeaders.Authorization]
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            val authStore = AuthStore(InMemoryTokenStorage())
            authStore.save("my-token", RecordModel(fields = UploadRecord()))
            service(engine, authStore).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertEquals("my-token", authHeader)
        }

    @Test
    fun createWithFilesSendsNoAuthHeaderWhenNoToken() =
        runTest {
            var authHeader: String? = "sentinel"
            val engine =
                MockEngine { request ->
                    authHeader = request.headers[HttpHeaders.Authorization]
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertNull(authHeader)
        }

    @Test
    fun createWithFilesReturnsDeserializedRecord() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val record = service(engine).createWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertEquals("r1", record.id)
        }

    @Test
    fun createWithFilesProgressCallbackReceivesValidValues() =
        runTest {
            val progressValues = mutableListOf<Float>()
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            service(engine).createWithFiles<UploadRecord>(
                buildJsonObject {},
                listOf(sampleFile()),
                onProgress = { progressValues.add(it) },
            )
            assertTrue(progressValues.isEmpty() || progressValues.all { it in 0f..1f })
        }

    // updateWithFiles

    @Test
    fun updateWithFilesSendsPatchMethod() =
        runTest {
            var method: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    method = request.method
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).updateWithFiles<UploadRecord>("r1", buildJsonObject {}, listOf(sampleFile()))
            assertEquals(HttpMethod.Patch, method)
        }

    @Test
    fun updateWithFilesSendsCorrectUrl() =
        runTest {
            var path: String? = null
            val engine =
                MockEngine { request ->
                    path = request.url.encodedPath
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).updateWithFiles<UploadRecord>("r1", buildJsonObject {}, listOf(sampleFile()))
            assertEquals("/api/collections/documents/records/r1", path)
        }

    @Test
    fun updateWithFilesSendsMultipartRequest() =
        runTest {
            var contentType: String? = null
            val engine =
                MockEngine { request ->
                    contentType = request.body.contentType?.toString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).updateWithFiles<UploadRecord>("r1", buildJsonObject {}, listOf(sampleFile()))
            assertNotNull(contentType)
            assertTrue(contentType!!.startsWith("multipart/form-data"))
        }

    @Test
    fun updateWithFilesReturnsDeserializedRecord() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val record = service(engine).updateWithFiles<UploadRecord>("r1", buildJsonObject {}, listOf(sampleFile()))
            assertEquals("r1", record.id)
        }

    // tryCreateWithFiles / tryUpdateWithFiles

    @Test
    fun tryCreateWithFilesReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).tryCreateWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            val record = result.getOrNull() ?: fail("expected success")
            assertEquals("r1", record.id)
        }

    @Test
    fun tryCreateWithFilesReturnsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("network error") }
            val result = service(engine).tryCreateWithFiles<UploadRecord>(buildJsonObject {}, listOf(sampleFile()))
            assertIs<PocketBaseResult.Failure>(result)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun tryUpdateWithFilesReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).tryUpdateWithFiles<UploadRecord>("r1", buildJsonObject {}, listOf(sampleFile()))
            val record = result.getOrNull() ?: fail("expected success")
            assertEquals("r1", record.id)
        }

    @Test
    fun tryUpdateWithFilesReturnsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("network error") }
            val result = service(engine).tryUpdateWithFiles<UploadRecord>("r1", buildJsonObject {}, listOf(sampleFile()))
            assertIs<PocketBaseResult.Failure>(result)
        }

    // getFileUrl

    @Test
    fun getFileUrlBuildsCorrectUrl() {
        val engine = MockEngine { respond("", HttpStatusCode.OK, headersOf()) }
        val url = service(engine).getFileUrl("r1", "report_abc123.pdf")
        assertEquals("$baseUrl/api/files/documents/r1/report_abc123.pdf", url)
    }

    @Test
    fun getFileUrlWithThumbAppendsQueryParam() {
        val engine = MockEngine { respond("", HttpStatusCode.OK, headersOf()) }
        val url = service(engine).getFileUrl("r1", "photo.jpg", thumb = "100x100")
        assertEquals("$baseUrl/api/files/documents/r1/photo.jpg?thumb=100x100", url)
    }

    @Test
    fun getFileUrlWithoutThumbHasNoQueryParam() {
        val engine = MockEngine { respond("", HttpStatusCode.OK, headersOf()) }
        val url = service(engine).getFileUrl("r1", "photo.jpg")
        assertTrue(!url.contains("?"))
    }

    // FileUpload data class

    @Test
    fun fileUploadDefaultMimeTypeIsOctetStream() {
        val file = FileUpload("field", "file.bin", byteArrayOf(1, 2, 3))
        assertEquals("application/octet-stream", file.mimeType)
    }

    @Test
    fun fileUploadEqualityBasedOnContent() {
        val bytes = "data".encodeToByteArray()
        val a = FileUpload("f", "name.txt", bytes, "text/plain")
        val b = FileUpload("f", "name.txt", bytes, "text/plain")
        assertEquals(a, b)
    }
}
