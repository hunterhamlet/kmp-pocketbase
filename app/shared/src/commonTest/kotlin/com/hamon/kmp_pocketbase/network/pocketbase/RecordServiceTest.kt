package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeService
import com.hamon.kmp_pocketbase.network.pocketbase.sort.asc
import com.hamon.kmp_pocketbase.network.pocketbase.sort.desc
import com.hamon.kmp_pocketbase.network.pocketbase.storage.InMemoryTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@Serializable
private data class TestRecord(
    val dummy: String? = null,
)

class RecordServiceTest {
    private val baseUrl = "https://test.pocketbase.io"
    private val collectionName = "users"
    private val recordsUrl = "$baseUrl/api/collections/$collectionName/records"

    private val recordJson =
        """{"id":"r1","collectionId":"col1","collectionName":"users","created":"2024-01-01","updated":"2024-01-01"}"""
    private val resultListJson =
        """{"page":1,"perPage":30,"totalItems":1,"totalPages":1,"items":[$recordJson]}"""
    private val authResponseJson =
        """{"token":"tok123","record":$recordJson}"""

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun service(
        engine: MockEngine,
        authStore: AuthStore = AuthStore(InMemoryTokenStorage()),
        logLevel: PocketBaseLogLevel = PocketBaseLogLevel.NONE,
    ): RecordService {
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                install(SSE)
            }
        return RecordService(
            client = client,
            baseUrl = baseUrl,
            collectionName = collectionName,
            authStore = authStore,
            log = PocketBaseLog(logLevel, PocketBaseLogger.Default),
            realtime = RealtimeService(client, baseUrl, authStore),
        )
    }

    @Test
    fun getListSendsPageAndPerPageParams() =
        runTest {
            var page: String? = null
            var perPage: String? = null
            val engine =
                MockEngine { request ->
                    page = request.url.parameters["page"]
                    perPage = request.url.parameters["perPage"]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getList<TestRecord>(page = 2, perPage = 15)
            assertEquals("2", page)
            assertEquals("15", perPage)
        }

    @Test
    fun getListWithFilterAppendsFilterParam() =
        runTest {
            var filter: String? = null
            val engine =
                MockEngine { request ->
                    filter = request.url.parameters["filter"]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getList<TestRecord>(
                query = pbQuery { filter { "status" eq "active" } },
            )
            assertEquals("status = \"active\"", filter)
        }

    @Test
    fun getListWithSortAppendsSortParam() =
        runTest {
            var sort: String? = null
            val engine =
                MockEngine { request ->
                    sort = request.url.parameters["sort"]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getList<TestRecord>(
                query =
                    pbQuery {
                        sort {
                            add("created".desc())
                            add("name".asc())
                        }
                    },
            )
            assertEquals("-created,+name", sort)
        }

    @Test
    fun getListWithExpandAndFieldsAppendsParams() =
        runTest {
            var expand: String? = null
            var fields: String? = null
            val engine =
                MockEngine { request ->
                    expand = request.url.parameters["expand"]
                    fields = request.url.parameters["fields"]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getList<TestRecord>(
                query =
                    pbQuery {
                        expand("profile")
                        fields("id", "name", "email")
                    },
            )
            assertEquals("profile", expand)
            assertEquals("id,name,email", fields)
        }

    @Test
    fun getListWithoutQuerySendsNoFilterParam() =
        runTest {
            var filter: String? = "sentinel"
            val engine =
                MockEngine { request ->
                    filter = request.url.parameters["filter"]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getList<TestRecord>()
            assertNull(filter)
        }

    @Test
    fun getListReturnsDeserializedResultList() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getList<TestRecord>()
            assertEquals(1, result.totalItems)
            assertEquals(1, result.items.size)
            assertEquals("r1", result.items.first().id)
        }

    @Test
    fun getFullListReturnsAllItemsFromSinglePage() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getFullList<TestRecord>()
            assertEquals(1, result.size)
            assertEquals("r1", result.first().id)
        }

    @Test
    fun getOneSendsCorrectUrl() =
        runTest {
            var capturedUrl: String? = null
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.encodedPath
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getOne<TestRecord>("r1")
            assertEquals("/api/collections/users/records/r1", capturedUrl)
        }

    @Test
    fun getOneWithExpandAppendsParam() =
        runTest {
            var expand: String? = null
            val engine =
                MockEngine { request ->
                    expand = request.url.parameters["expand"]
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getOne<TestRecord>("r1", expand = "profile")
            assertEquals("profile", expand)
        }

    @Test
    fun createSendsPostWithJsonBody() =
        runTest {
            var method: HttpMethod? = null
            var bodyText: String? = null
            val engine =
                MockEngine { request ->
                    method = request.method
                    bodyText = request.body.toByteArray().decodeToString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            val body =
                kotlinx.serialization.json.buildJsonObject {
                    put(
                        "name",
                        kotlinx.serialization.json.JsonPrimitive("Alice"),
                    )
                }
            service(engine).create<TestRecord>(body)
            assertEquals(HttpMethod.Post, method)
            val parsed = Json.parseToJsonElement(bodyText!!).jsonObject
            assertEquals("Alice", parsed["name"]?.jsonPrimitive?.content)
        }

    @Test
    fun updateSendsPatchWithJsonBody() =
        runTest {
            var method: HttpMethod? = null
            var bodyText: String? = null
            val engine =
                MockEngine { request ->
                    method = request.method
                    bodyText = request.body.toByteArray().decodeToString()
                    respond(recordJson, HttpStatusCode.OK, jsonHeaders())
                }
            val body =
                kotlinx.serialization.json.buildJsonObject {
                    put(
                        "name",
                        kotlinx.serialization.json.JsonPrimitive("Bob"),
                    )
                }
            service(engine).update<TestRecord>("r1", body)
            assertEquals(HttpMethod.Patch, method)
            val parsed = Json.parseToJsonElement(bodyText!!).jsonObject
            assertEquals("Bob", parsed["name"]?.jsonPrimitive?.content)
        }

    @Test
    fun deleteSendsDeleteMethod() =
        runTest {
            var method: HttpMethod? = null
            var capturedUrl: String? = null
            val engine =
                MockEngine { request ->
                    method = request.method
                    capturedUrl = request.url.encodedPath
                    respond("", HttpStatusCode.NoContent, headersOf())
                }
            service(engine).delete("r1")
            assertEquals(HttpMethod.Delete, method)
            assertEquals("/api/collections/users/records/r1", capturedUrl)
        }

    @Test
    fun authWithPasswordSavesTokenInAuthStore() =
        runTest {
            val engine = MockEngine { respond(authResponseJson, HttpStatusCode.OK, jsonHeaders()) }
            val authStore = AuthStore(InMemoryTokenStorage())
            service(engine, authStore).authWithPassword<TestRecord>("user@test.com", "secret")
            assertTrue(authStore.isValid)
            assertEquals("tok123", authStore.token)
        }

    @Test
    fun authWithPasswordSendsIdentityAndPassword() =
        runTest {
            var bodyText: String? = null
            val engine =
                MockEngine { request ->
                    bodyText = request.body.toByteArray().decodeToString()
                    respond(authResponseJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).authWithPassword<TestRecord>("user@test.com", "secret123")
            val body = Json.parseToJsonElement(bodyText!!).jsonObject
            assertEquals("user@test.com", body["identity"]?.jsonPrimitive?.content)
            assertEquals("secret123", body["password"]?.jsonPrimitive?.content)
        }

    @Test
    fun authorizationHeaderIsSentWhenTokenPresent() =
        runTest {
            var authHeader: String? = null
            val engine =
                MockEngine { request ->
                    authHeader = request.headers[HttpHeaders.Authorization]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            val authStore = AuthStore(InMemoryTokenStorage())
            authStore.save(
                "my-token",
                RecordModel(fields = TestRecord()),
            )
            service(engine, authStore).getList<TestRecord>()
            assertEquals("my-token", authHeader)
        }

    @Test
    fun noAuthorizationHeaderWhenTokenAbsent() =
        runTest {
            var authHeader: String? = "sentinel"
            val engine =
                MockEngine { request ->
                    authHeader = request.headers[HttpHeaders.Authorization]
                    respond(resultListJson, HttpStatusCode.OK, jsonHeaders())
                }
            service(engine).getList<TestRecord>()
            assertNull(authHeader)
        }

    @Test
    fun tryGetListReturnsSuccessWithCorrectData() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val data =
                service(engine).tryGetList<TestRecord>().getOrNull() ?: fail("expected success")
            assertEquals(1, data.totalItems)
            assertEquals("r1", data.items.first().id)
        }

    @Test
    fun tryGetListReturnsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryGetList<TestRecord>()
            assertIs<PocketBaseResult.Failure>(result)
            assertNotNull(result.exceptionOrNull())
        }

    @Test
    fun tryGetOneReturnsSuccessWithRecord() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val record =
                service(engine).tryGetOne<TestRecord>("r1").getOrNull() ?: fail("expected success")
            assertEquals("r1", record.id)
        }

    @Test
    fun tryGetOneReturnsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryGetOne<TestRecord>("r1")
            assertIs<PocketBaseResult.Failure>(result)
        }

    @Test
    fun getListAsFlowEmitsSingleResult() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getListAsFlow<TestRecord>().first()
            assertEquals(1, result.totalItems)
            assertEquals("r1", result.items.first().id)
        }

    @Test
    fun tryGetListAsFlowEmitsSuccessOnOk() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val data =
                service(engine).tryGetListAsFlow<TestRecord>().first().getOrNull()
                    ?: fail("expected success")
            assertEquals("r1", data.items.first().id)
        }

    @Test
    fun tryGetListAsFlowEmitsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryGetListAsFlow<TestRecord>().first()
            assertIs<PocketBaseResult.Failure>(result)
        }

    @Test
    fun tryDeleteReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond("", HttpStatusCode.NoContent, headersOf()) }
            val result = service(engine).tryDelete("r1")
            assertTrue(result is PocketBaseResult.Success)
        }

    @Test
    fun getFullListPaginatesAcrossMultiplePages() =
        runTest {
            val page1Json =
                """{"page":1,"perPage":2,"totalItems":3,"totalPages":2,"items":[""" +
                    """{"id":"r1","collectionId":"c","collectionName":"users","created":"","updated":""},""" +
                    """{"id":"r2","collectionId":"c","collectionName":"users","created":"","updated":""}]}"""
            val page2Json =
                """{"page":2,"perPage":2,"totalItems":3,"totalPages":2,"items":[""" +
                    """{"id":"r3","collectionId":"c","collectionName":"users","created":"","updated":""}]}"""
            val engine =
                MockEngine { request ->
                    val json = if (request.url.parameters["page"] == "1") page1Json else page2Json
                    respond(json, HttpStatusCode.OK, jsonHeaders())
                }
            val result = service(engine).getFullList<TestRecord>()
            assertEquals(3, result.size)
            assertEquals("r1", result[0].id)
            assertEquals("r2", result[1].id)
            assertEquals("r3", result[2].id)
        }

    @Test
    fun tryGetFullListReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val data =
                service(engine).tryGetFullList<TestRecord>().getOrNull() ?: fail("expected success")
            assertEquals(1, data.size)
            assertEquals("r1", data.first().id)
        }

    @Test
    fun tryGetFullListReturnsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryGetFullList<TestRecord>()
            assertIs<PocketBaseResult.Failure>(result)
        }

    @Test
    fun getFullListAsFlowEmitsAllItems() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getFullListAsFlow<TestRecord>().first()
            assertEquals(1, result.size)
            assertEquals("r1", result.first().id)
        }

    @Test
    fun tryGetFullListAsFlowEmitsSuccessOnOk() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val data =
                service(engine).tryGetFullListAsFlow<TestRecord>().first().getOrNull()
                    ?: fail("expected success")
            assertEquals("r1", data.first().id)
        }

    @Test
    fun tryGetFullListAsFlowEmitsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryGetFullListAsFlow<TestRecord>().first()
            assertIs<PocketBaseResult.Failure>(result)
        }

    @Test
    fun getOneAsFlowEmitsRecord() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getOneAsFlow<TestRecord>("r1").first()
            assertEquals("r1", result.id)
        }

    @Test
    fun tryGetOneAsFlowEmitsSuccess() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val record =
                service(engine).tryGetOneAsFlow<TestRecord>("r1").first().getOrNull()
                    ?: fail("expected success")
            assertEquals("r1", record.id)
        }

    @Test
    fun tryGetOneAsFlowEmitsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryGetOneAsFlow<TestRecord>("r1").first()
            assertIs<PocketBaseResult.Failure>(result)
        }

    @Test
    fun tryCreateReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val record =
                service(engine)
                    .tryCreate<TestRecord>(kotlinx.serialization.json.buildJsonObject {})
                    .getOrNull() ?: fail("expected success")
            assertEquals("r1", record.id)
        }

    @Test
    fun tryUpdateReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val record =
                service(engine)
                    .tryUpdate<TestRecord>("r1", kotlinx.serialization.json.buildJsonObject {})
                    .getOrNull() ?: fail("expected success")
            assertEquals("r1", record.id)
        }

    @Test
    fun tryDeleteReturnsFailureOnNetworkError() =
        runTest {
            val engine = MockEngine { throw RuntimeException("simulated error") }
            val result = service(engine).tryDelete("r1")
            assertIs<PocketBaseResult.Failure>(result)
        }

    @Test
    fun tryAuthWithPasswordReturnsSuccess() =
        runTest {
            val engine = MockEngine { respond(authResponseJson, HttpStatusCode.OK, jsonHeaders()) }
            val auth =
                service(engine)
                    .tryAuthWithPassword<TestRecord>("user@test.com", "pw")
                    .getOrNull() ?: fail("expected success")
            assertEquals("tok123", auth.token)
            assertEquals("r1", auth.record.id)
        }

    @Test
    fun getListLogsResponseWhenLoggingEnabled() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine, logLevel = PocketBaseLogLevel.HEADERS).getList<TestRecord>()
            assertEquals(1, result.totalItems)
        }

    @Test
    fun getOneLogsResponseWhenLoggingEnabled() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine, logLevel = PocketBaseLogLevel.HEADERS).getOne<TestRecord>("r1")
            assertEquals("r1", result.id)
        }

    @Test
    fun createLogsResponseWhenLoggingEnabled() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val result =
                service(engine, logLevel = PocketBaseLogLevel.HEADERS)
                    .create<TestRecord>(kotlinx.serialization.json.buildJsonObject {})
            assertEquals("r1", result.id)
        }

    @Test
    fun updateLogsResponseWhenLoggingEnabled() =
        runTest {
            val engine = MockEngine { respond(recordJson, HttpStatusCode.OK, jsonHeaders()) }
            val result =
                service(engine, logLevel = PocketBaseLogLevel.HEADERS)
                    .update<TestRecord>("r1", kotlinx.serialization.json.buildJsonObject {})
            assertEquals("r1", result.id)
        }

    @Test
    fun deleteLogsResponseWhenLoggingEnabled() =
        runTest {
            val engine = MockEngine { respond("", HttpStatusCode.NoContent, headersOf()) }
            service(engine, logLevel = PocketBaseLogLevel.HEADERS).delete("r1")
        }

    @Test
    fun authWithPasswordLogsResponseWhenLoggingEnabled() =
        runTest {
            val engine = MockEngine { respond(authResponseJson, HttpStatusCode.OK, jsonHeaders()) }
            val auth =
                service(engine, logLevel = PocketBaseLogLevel.HEADERS)
                    .authWithPassword<TestRecord>("user@test.com", "pw")
            assertEquals("tok123", auth.token)
        }

    @Test
    fun subscribeCollectionReturnsNonNullFlow() {
        val engine = MockEngine { respond("", HttpStatusCode.OK, headersOf()) }
        assertNotNull(service(engine).subscribe<TestRecord>())
    }

    @Test
    fun subscribeRecordReturnsNonNullFlow() {
        val engine = MockEngine { respond("", HttpStatusCode.OK, headersOf()) }
        assertNotNull(service(engine).subscribe<TestRecord>("r1"))
    }
}
