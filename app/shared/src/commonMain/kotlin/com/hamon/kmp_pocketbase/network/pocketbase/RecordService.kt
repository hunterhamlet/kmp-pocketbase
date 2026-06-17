package com.hamon.kmp_pocketbase.network.pocketbase

import com.hamon.kmp_pocketbase.network.pocketbase.dto.AuthResponse
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.dto.ResultList
import com.hamon.kmp_pocketbase.network.pocketbase.dto.toRecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.dto.toResultList
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeEvent
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeService
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer

@Suppress("TooManyFunctions")
class RecordService internal constructor(
    private val client: HttpClient,
    private val baseUrl: String,
    @PublishedApi internal val collectionName: String,
    private val authStore: AuthStore,
    private val log: PocketBaseLog,
    private val realtime: RealtimeService,
) {
    private val baseRecordsUrl get() = "$baseUrl/api/collections/$collectionName/records"

    @PublishedApi
    internal suspend fun <T> getListInternal(
        page: Int,
        perPage: Int,
        query: QueryParams?,
        serializer: KSerializer<T>,
    ): ResultList<T> =
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
            }.decodeAsResultList(serializer)

    @Generated
    suspend inline fun <reified T> getList(
        page: Int = 1,
        perPage: Int = 30,
        query: QueryParams? = null,
    ): ResultList<T> = getListInternal(page, perPage, query, serializer())

    @Generated
    suspend inline fun <reified T> tryGetList(
        page: Int = 1,
        perPage: Int = 30,
        query: QueryParams? = null,
    ): PocketBaseResult<ResultList<T>> {
        val s = serializer<T>()
        return safeSuspend { getListInternal(page, perPage, query, s) }
    }

    @Generated
    inline fun <reified T> getListAsFlow(
        page: Int = 1,
        perPage: Int = 30,
        query: QueryParams? = null,
    ): Flow<ResultList<T>> {
        val s = serializer<T>()
        return flow { emit(getListInternal(page, perPage, query, s)) }
    }

    @Generated
    inline fun <reified T> tryGetListAsFlow(
        page: Int = 1,
        perPage: Int = 30,
        query: QueryParams? = null,
    ): Flow<PocketBaseResult<ResultList<T>>> {
        val s = serializer<T>()
        return flow { emit(safeSuspend { getListInternal(page, perPage, query, s) }) }
    }

    @PublishedApi
    internal suspend fun <T> getFullListInternal(
        perPage: Int,
        query: QueryParams?,
        serializer: KSerializer<T>,
    ): List<RecordModel<T>> {
        val results = mutableListOf<RecordModel<T>>()
        var page = 1
        while (true) {
            val batch = getListInternal(page, perPage, query, serializer)
            results.addAll(batch.items)
            if (page >= batch.totalPages) break
            page++
        }
        return results
    }

    @Generated
    suspend inline fun <reified T> getFullList(
        perPage: Int = 200,
        query: QueryParams? = null,
    ): List<RecordModel<T>> = getFullListInternal(perPage, query, serializer())

    @Generated
    suspend inline fun <reified T> tryGetFullList(
        perPage: Int = 200,
        query: QueryParams? = null,
    ): PocketBaseResult<List<RecordModel<T>>> {
        val s = serializer<T>()
        return safeSuspend { getFullListInternal(perPage, query, s) }
    }

    @Generated
    inline fun <reified T> getFullListAsFlow(
        perPage: Int = 200,
        query: QueryParams? = null,
    ): Flow<List<RecordModel<T>>> {
        val s = serializer<T>()
        return flow { emit(getFullListInternal(perPage, query, s)) }
    }

    @Generated
    inline fun <reified T> tryGetFullListAsFlow(
        perPage: Int = 200,
        query: QueryParams? = null,
    ): Flow<PocketBaseResult<List<RecordModel<T>>>> {
        val s = serializer<T>()
        return flow { emit(safeSuspend { getFullListInternal(perPage, query, s) }) }
    }

    @PublishedApi
    internal suspend fun <T> getOneInternal(
        id: String,
        expand: String?,
        fields: String?,
        serializer: KSerializer<T>,
    ): RecordModel<T> =
        client
            .get("$baseRecordsUrl/$id") {
                url {
                    expand?.let { parameters.append("expand", it) }
                    fields?.let { parameters.append("fields", it) }
                }
                authStore.token?.let { header("Authorization", it) }
            }.decodeAsRecord(serializer)

    @Generated
    suspend inline fun <reified T> getOne(
        id: String,
        expand: String? = null,
        fields: String? = null,
    ): RecordModel<T> = getOneInternal(id, expand, fields, serializer())

    @Generated
    suspend inline fun <reified T> tryGetOne(
        id: String,
        expand: String? = null,
        fields: String? = null,
    ): PocketBaseResult<RecordModel<T>> {
        val s = serializer<T>()
        return safeSuspend { getOneInternal(id, expand, fields, s) }
    }

    @Generated
    inline fun <reified T> getOneAsFlow(
        id: String,
        expand: String? = null,
        fields: String? = null,
    ): Flow<RecordModel<T>> {
        val s = serializer<T>()
        return flow { emit(getOneInternal(id, expand, fields, s)) }
    }

    @Generated
    inline fun <reified T> tryGetOneAsFlow(
        id: String,
        expand: String? = null,
        fields: String? = null,
    ): Flow<PocketBaseResult<RecordModel<T>>> {
        val s = serializer<T>()
        return flow { emit(safeSuspend { getOneInternal(id, expand, fields, s) }) }
    }

    @PublishedApi
    internal suspend fun <T> createInternal(
        body: JsonObject,
        serializer: KSerializer<T>,
    ): RecordModel<T> =
        client
            .post(baseRecordsUrl) {
                contentType(ContentType.Application.Json)
                setBody(body)
                authStore.token?.let { header("Authorization", it) }
            }.decodeAsRecord(serializer, requestBody = body.toString())

    @Generated
    suspend inline fun <reified T> create(body: JsonObject): RecordModel<T> = createInternal(body, serializer())

    @Generated
    suspend inline fun <reified T> tryCreate(body: JsonObject): PocketBaseResult<RecordModel<T>> {
        val s = serializer<T>()
        return safeSuspend { createInternal(body, s) }
    }

    @PublishedApi
    internal suspend fun <T> updateInternal(
        id: String,
        body: JsonObject,
        serializer: KSerializer<T>,
    ): RecordModel<T> =
        client
            .patch("$baseRecordsUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(body)
                authStore.token?.let { header("Authorization", it) }
            }.decodeAsRecord(serializer, requestBody = body.toString())

    @Generated
    suspend inline fun <reified T> update(
        id: String,
        body: JsonObject,
    ): RecordModel<T> = updateInternal(id, body, serializer())

    @Generated
    suspend inline fun <reified T> tryUpdate(
        id: String,
        body: JsonObject,
    ): PocketBaseResult<RecordModel<T>> {
        val s = serializer<T>()
        return safeSuspend { updateInternal(id, body, s) }
    }

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

    suspend fun tryDelete(id: String): PocketBaseResult<Unit> = safeSuspend { delete(id) }

    @PublishedApi
    internal suspend fun <T> authWithPasswordInternal(
        identity: String,
        password: String,
        serializer: KSerializer<T>,
    ): AuthResponse<T> {
        val authUrl = "$baseUrl/api/collections/$collectionName/auth-with-password"
        val payload =
            buildJsonObject {
                put("identity", identity)
                put("password", password)
            }
        val jsonObject =
            client
                .post(authUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }.decodeAsJsonObject(requestBody = payload.toString())
        val token = jsonObject["token"]?.jsonPrimitive?.content ?: ""
        val record =
            jsonObject["record"]?.jsonObject?.toRecordModel(serializer)
                ?: error("missing record in auth response")
        authStore.save(token, record)
        return AuthResponse(token = token, record = record)
    }

    @Generated
    suspend inline fun <reified T> authWithPassword(
        identity: String,
        password: String,
    ): AuthResponse<T> = authWithPasswordInternal(identity, password, serializer())

    @Generated
    suspend inline fun <reified T> tryAuthWithPassword(
        identity: String,
        password: String,
    ): PocketBaseResult<AuthResponse<T>> {
        val s = serializer<T>()
        return safeSuspend { authWithPasswordInternal(identity, password, s) }
    }

    @PublishedApi
    internal suspend fun <T> authRefreshInternal(
        expand: String?,
        fields: String?,
        serializer: KSerializer<T>,
    ): AuthResponse<T> {
        val refreshUrl = "$baseUrl/api/collections/$collectionName/auth-refresh"
        val jsonObject =
            client
                .post(refreshUrl) {
                    url {
                        expand?.let { parameters.append("expand", it) }
                        fields?.let { parameters.append("fields", it) }
                    }
                    authStore.token?.let { header("Authorization", it) }
                }.decodeAsJsonObject()
        val token = jsonObject["token"]?.jsonPrimitive?.content ?: ""
        val record =
            jsonObject["record"]?.jsonObject?.toRecordModel(serializer)
                ?: error("missing record in auth-refresh response")
        authStore.save(token, record)
        return AuthResponse(token = token, record = record)
    }

    @Generated
    suspend inline fun <reified T> authRefresh(
        expand: String? = null,
        fields: String? = null,
    ): AuthResponse<T> = authRefreshInternal(expand, fields, serializer())

    @Generated
    suspend inline fun <reified T> tryAuthRefresh(
        expand: String? = null,
        fields: String? = null,
    ): PocketBaseResult<AuthResponse<T>> {
        val s = serializer<T>()
        return safeSuspend { authRefreshInternal(expand, fields, s) }
    }

    @PublishedApi
    internal fun <T> subscribeInternal(
        topic: String,
        autoReconnect: Boolean,
        serializer: KSerializer<T>,
    ): Flow<PocketBaseResult<RealtimeEvent<T>>> = realtime.subscribe(topic, autoReconnect, serializer)

    @Generated
    inline fun <reified T> subscribe(autoReconnect: Boolean = false): Flow<PocketBaseResult<RealtimeEvent<T>>> =
        subscribeInternal(collectionName, autoReconnect, serializer())

    @Generated
    inline fun <reified T> subscribe(
        recordId: String,
        autoReconnect: Boolean = false,
    ): Flow<PocketBaseResult<RealtimeEvent<T>>> =
        subscribeInternal("$collectionName/$recordId", autoReconnect, serializer())

    private suspend fun <T> HttpResponse.decodeAsRecord(
        serializer: KSerializer<T>,
        requestBody: String? = null,
    ): RecordModel<T> {
        if (!log.isEnabled()) return body<JsonObject>().toRecordModel(serializer)
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
        return pocketBaseJson.decodeFromString<JsonObject>(text).toRecordModel(serializer)
    }

    private suspend fun <T> HttpResponse.decodeAsResultList(serializer: KSerializer<T>): ResultList<T> {
        if (!log.isEnabled()) return body<JsonObject>().toResultList(serializer)
        val text = bodyAsText()
        log.log(
            url = call.request.url.toString(),
            collection = collectionName,
            token = authStore.token,
            requestHeaders = requestHeadersMap(),
            requestBody = null,
            status = "${status.value} ${status.description}",
            responseHeaders = responseHeadersMap(),
            responseBody = text,
        )
        return pocketBaseJson.decodeFromString<JsonObject>(text).toResultList(serializer)
    }

    private suspend fun HttpResponse.decodeAsJsonObject(requestBody: String? = null): JsonObject {
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
