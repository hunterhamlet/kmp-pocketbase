package com.hamon.kmp_pocketbase.network.pocketbase.realtime

import com.hamon.kmp_pocketbase.network.pocketbase.AuthStore
import com.hamon.kmp_pocketbase.network.pocketbase.storage.InMemoryTokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Serializable
private data class TestFields(
    val name: String = "",
)

class RealtimeServiceTest {
    private val service =
        RealtimeService(
            client = HttpClient(MockEngine { respond("", HttpStatusCode.OK, headersOf()) }),
            baseUrl = "https://test.pocketbase.io",
            authStore = AuthStore(InMemoryTokenStorage()),
        )

    @Test
    fun parseClientIdExtractsClientId() {
        val result = service.parseClientId("""{"clientId":"abc123"}""")
        assertEquals("abc123", result)
    }

    @Test
    fun parseClientIdReturnsNullOnInvalidJson() {
        assertNull(service.parseClientId("not valid json"))
    }

    @Test
    fun parseClientIdReturnsNullOnMissingClientIdField() {
        assertNull(service.parseClientId("""{"other":"value"}"""))
    }

    @Test
    fun parseEventReturnsCreateAction() {
        val json =
            """{"action":"create","record":{"id":"r1","collectionId":"c",""" +
                """"collectionName":"users","created":"","updated":""}}"""
        val event = service.parseEvent(json, serializer<TestFields>())
        assertNotNull(event)
        assertEquals(RealtimeAction.CREATE, event.action)
        assertEquals("r1", event.record.id)
    }

    @Test
    fun parseEventReturnsUpdateAction() {
        val json =
            """{"action":"update","record":{"id":"r2","collectionId":"c",""" +
                """"collectionName":"users","created":"","updated":""}}"""
        val event = service.parseEvent(json, serializer<TestFields>())
        assertNotNull(event)
        assertEquals(RealtimeAction.UPDATE, event.action)
        assertEquals("r2", event.record.id)
    }

    @Test
    fun parseEventReturnsDeleteAction() {
        val json =
            """{"action":"delete","record":{"id":"r3","collectionId":"c",""" +
                """"collectionName":"users","created":"","updated":""}}"""
        val event = service.parseEvent(json, serializer<TestFields>())
        assertNotNull(event)
        assertEquals(RealtimeAction.DELETE, event.action)
        assertEquals("r3", event.record.id)
    }

    @Test
    fun parseEventReturnsNullForUnknownAction() {
        val json =
            """{"action":"unknown","record":{"id":"r1","collectionId":"c",""" +
                """"collectionName":"users","created":"","updated":""}}"""
        assertNull(service.parseEvent(json, serializer<TestFields>()))
    }

    @Test
    fun parseEventReturnsNullOnInvalidJson() {
        assertNull(service.parseEvent("not valid json", serializer<TestFields>()))
    }
}
