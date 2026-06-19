package com.hamon.kmp_pocketbase.demo.chat

import com.hamon.kmp_pocketbase.generated.MessagesRecord
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBase
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class PocketBaseChatRepository(
    private val pb: PocketBase,
) : ChatRepository {
    private val service = pb.collection("messages")

    override fun subscribeToMessages(): Flow<PocketBaseResult<RealtimeEvent<MessagesRecord>>> =
        service.subscribe<MessagesRecord>(autoReconnect = true)

    override suspend fun sendMessage(text: String): PocketBaseResult<RecordModel<MessagesRecord>> =
        service.tryCreate(
            buildJsonObject {
                put("text", text)
                put("author", pb.authStore.model?.id ?: "")
            },
        )

    override suspend fun getRecentMessages(): PocketBaseResult<List<RecordModel<MessagesRecord>>> =
        service.tryGetFullList()
}
