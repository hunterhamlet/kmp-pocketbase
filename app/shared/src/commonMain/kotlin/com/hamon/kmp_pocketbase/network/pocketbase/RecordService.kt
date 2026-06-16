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
) {
    private val baseRecordsUrl get() = "$baseUrl/api/collections/$collectionName/records"

    suspend fun getList(
        page: Int = 1,
        perPage: Int = 30,
    ): ResultList =
        client
            .get(baseRecordsUrl) {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("perPage", perPage.toString())
                }
                authStore.token?.let { header("Authorization", it) }
            }.body()

    suspend fun getFullList(perPage: Int = 200): List<RecordModel> {
        val results = mutableListOf<RecordModel>()
        var page = 1
        while (true) {
            val batch = getList(page = page, perPage = perPage)
            results.addAll(batch.items)
            if (page >= batch.totalPages) break
            page++
        }
        return results
    }

    suspend fun getOne(id: String): RecordModel =
        client
            .get("$baseRecordsUrl/$id") {
                authStore.token?.let { header("Authorization", it) }
            }.body()

    suspend fun create(body: JsonObject): RecordModel =
        client
            .post(baseRecordsUrl) {
                contentType(ContentType.Application.Json)
                setBody(body)
                authStore.token?.let { header("Authorization", it) }
            }.body()

    suspend fun update(
        id: String,
        body: JsonObject,
    ): RecordModel =
        client
            .patch("$baseRecordsUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(body)
                authStore.token?.let { header("Authorization", it) }
            }.body()

    suspend fun delete(id: String) {
        client.delete("$baseRecordsUrl/$id") {
            authStore.token?.let { header("Authorization", it) }
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
                }.body()
        authStore.save(response.token, response.record)
        return response
    }
}
