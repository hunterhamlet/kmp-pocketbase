package com.hamon.kmp_pocketbase.network.pocketbase.realtime

import com.hamon.kmp_pocketbase.network.pocketbase.AuthStore
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.toRecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.pocketBaseJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.serverSentEvents
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

private const val RETRY_DELAY_MS = 3_000L

@Serializable
private data class ClientIdMessage(
    val clientId: String,
)

@Serializable
private data class SubscribeRequest(
    val clientId: String,
    val subscriptions: List<String>,
)

@Serializable
private data class RawRealtimeEvent(
    val action: String = "",
    val record: JsonObject = JsonObject(emptyMap()),
)

internal class RealtimeService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val authStore: AuthStore,
) {
    @Suppress("TooGenericExceptionCaught")
    fun <T> subscribe(
        topic: String,
        autoReconnect: Boolean,
        serializer: KSerializer<T>,
    ): Flow<PocketBaseResult<RealtimeEvent<T>>> =
        channelFlow {
            while (isActive) {
                try {
                    connectAndEmit(topic, serializer)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    send(PocketBaseResult.Failure(e))
                    if (!autoReconnect || !isActive) break
                }
                if (!autoReconnect || !isActive) break
                delay(RETRY_DELAY_MS)
            }
        }

    private suspend fun <T> ProducerScope<PocketBaseResult<RealtimeEvent<T>>>.connectAndEmit(
        topic: String,
        serializer: KSerializer<T>,
    ) {
        client.serverSentEvents("$baseUrl/api/realtime") {
            var subscribed = false
            incoming.collect { event ->
                val data = event.data?.takeIf { it.isNotBlank() } ?: return@collect
                if (!subscribed) {
                    parseClientId(data)?.let { clientId ->
                        postSubscribe(clientId, topic)
                        subscribed = true
                    }
                } else {
                    parseEvent(data, serializer)?.let { send(PocketBaseResult.Success(it)) }
                }
            }
        }
    }

    private suspend fun postSubscribe(
        clientId: String,
        topic: String,
    ) {
        client.post("$baseUrl/api/realtime") {
            contentType(ContentType.Application.Json)
            setBody(SubscribeRequest(clientId = clientId, subscriptions = listOf(topic)))
            authStore.token?.let { header("Authorization", it) }
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    internal fun parseClientId(data: String): String? =
        try {
            pocketBaseJson.decodeFromString<ClientIdMessage>(data).clientId
        } catch (e: Exception) {
            null
        }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    internal fun <T> parseEvent(
        data: String,
        serializer: KSerializer<T>,
    ): RealtimeEvent<T>? =
        try {
            val raw = pocketBaseJson.decodeFromString<RawRealtimeEvent>(data)
            val action =
                when (raw.action) {
                    "create" -> RealtimeAction.CREATE
                    "update" -> RealtimeAction.UPDATE
                    "delete" -> RealtimeAction.DELETE
                    else -> return null
                }
            RealtimeEvent(action = action, record = raw.record.toRecordModel(serializer))
        } catch (e: Exception) {
            null
        }
}
