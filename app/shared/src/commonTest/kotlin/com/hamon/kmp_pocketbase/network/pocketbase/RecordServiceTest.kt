package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.sort.asc
import com.hamon.kmp_pocketbase.network.pocketbase.sort.desc
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        authStore: AuthStore = AuthStore(),
    ): RecordService {
        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        return RecordService(
            client = client,
            baseUrl = baseUrl,
            collectionName = collectionName,
            authStore = authStore,
            log = PocketBaseLog(PocketBaseLogLevel.NONE, PocketBaseLogger.Default),
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
            service(engine).getList(page = 2, perPage = 15)
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
            service(engine).getList(
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
            service(engine).getList(
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
            service(engine).getList(
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
            service(engine).getList()
            assertNull(filter)
        }

    @Test
    fun getListReturnsDeserializedResultList() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getList()
            assertEquals(1, result.totalItems)
            assertEquals(1, result.items.size)
            assertEquals("r1", result.items.first().id)
        }

    @Test
    fun getFullListReturnsAllItemsFromSinglePage() =
        runTest {
            val engine = MockEngine { respond(resultListJson, HttpStatusCode.OK, jsonHeaders()) }
            val result = service(engine).getFullList()
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
            service(engine).getOne("r1")
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
            service(engine).getOne("r1", expand = "profile")
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
            service(engine).create(body)
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
            service(engine).update("r1", body)
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
            val authStore = AuthStore()
            service(engine, authStore).authWithPassword("user@test.com", "secret")
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
            service(engine).authWithPassword("user@test.com", "secret123")
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
            val authStore = AuthStore()
            authStore.save(
                "my-token",
                com.hamon.kmp_pocketbase.network.pocketbase.dto
                    .RecordModel(),
            )
            service(engine, authStore).getList()
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
            service(engine).getList()
            assertNull(authHeader)
        }
}
