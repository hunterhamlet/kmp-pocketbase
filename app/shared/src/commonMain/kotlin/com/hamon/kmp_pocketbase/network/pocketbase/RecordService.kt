package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.AuthResponse
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.dto.ResultList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RecordService internal constructor(
    private val client: HttpClient,
    private val baseUrl: String,
    private val collectionName: String,
    private val authStore: AuthStore,
    private val log: PocketBaseLog,
) {
    private val baseRecordsUrl get() = "$baseUrl/api/collections/$collectionName/records"

    suspend fun getList(
        page: Int = 1,
        perPage: Int = 30,
        query: QueryParams? = null,
    ): ResultList =
        client
            .get(baseRecordsUrl) {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("perPage", perPage.toString())
                    query?.filter?.let { parameters.append("filter", it) }
                    query?.sort?.let { parameters.append("sort", it) }
                    query?.expand?.let { parameters.append("expand", it) }
                    query?.fields?.let { parameters.append("fields", it) }
                    if (query?.skipTotal == true) parameters.append("skipTotal", "1")
                }
                authStore.token?.let { header("Authorization", it) }
            }.logAndDecode()

    suspend fun getFullList(
        perPage: Int = 200,
        query: QueryParams? = null,
    ): List<RecordModel> {
        val results = mutableListOf<RecordModel>()
        var page = 1
        while (true) {
            val batch = getList(page = page, perPage = perPage, query = query)
            results.addAll(batch.items)
            if (page >= batch.totalPages) break
            page++
        }
        return results
    }

    suspend fun getOne(
        id: String,
        expand: String? = null,
        fields: String? = null,
    ): RecordModel =
        client
            .get("$baseRecordsUrl/$id") {
                url {
                    expand?.let { parameters.append("expand", it) }
                    fields?.let { parameters.append("fields", it) }
                }
                authStore.token?.let { header("Authorization", it) }
            }.logAndDecode()

    suspend fun create(body: JsonObject): RecordModel =
        client
            .post(baseRecordsUrl) {
                contentType(ContentType.Application.Json)
                setBody(body)
                authStore.token?.let { header("Authorization", it) }
            }.logAndDecode(requestBody = body.toString())

    suspend fun update(
        id: String,
        body: JsonObject,
    ): RecordModel =
        client
            .patch("$baseRecordsUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(body)
                authStore.token?.let { header("Authorization", it) }
            }.logAndDecode(requestBody = body.toString())

    suspend fun delete(id: String) {
        val response =
            client.delete("$baseRecordsUrl/$id") {
                authStore.token?.let { header("Authorization", it) }
            }
        if (log.isEnabled()) {
            log.log(
                url =
                    response.call.request.url
                        .toString(),
                collection = collectionName,
                token = authStore.token,
                requestHeaders = response.requestHeadersMap(),
                requestBody = null,
                status = "${response.status.value} ${response.status.description}",
                responseHeaders = response.responseHeadersMap(),
                responseBody = response.bodyAsText().ifEmpty { "(empty)" },
            )
        }
    }

    suspend fun authWithPassword(
        identity: String,
        password: String,
    ): AuthResponse {
        val authUrl = "$baseUrl/api/collections/$collectionName/auth-with-password"
        val payload =
            buildJsonObject {
                put("identity", identity)
                put("password", password)
            }
        val response: AuthResponse =
            client
                .post(authUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }.logAndDecode(requestBody = payload.toString())
        authStore.save(response.token, response.record)
        return response
    }

    private suspend inline fun <reified T> HttpResponse.logAndDecode(requestBody: String? = null): T {
        if (!log.isEnabled()) return body()
        val text = bodyAsText()
        log.log(
            url = call.request.url.toString(),
            collection = collectionName,
            token = authStore.token,
            requestHeaders = requestHeadersMap(),
            requestBody = requestBody,
            status = "${status.value} ${status.description}",
            responseHeaders = responseHeadersMap(),
            responseBody = text,
        )
        return pocketBaseJson.decodeFromString(text)
    }

    private fun HttpResponse.requestHeadersMap(): Map<String, String>? =
        if (log.level >= PocketBaseLogLevel.HEADERS) {
            call.request.headers
                .entries()
                .associate { entry -> entry.key to entry.value.joinToString(", ") }
        } else {
            null
        }

    private fun HttpResponse.responseHeadersMap(): Map<String, String>? =
        if (log.level >= PocketBaseLogLevel.HEADERS) {
            headers
                .entries()
                .associate { entry -> entry.key to entry.value.joinToString(", ") }
        } else {
            null
        }
}
