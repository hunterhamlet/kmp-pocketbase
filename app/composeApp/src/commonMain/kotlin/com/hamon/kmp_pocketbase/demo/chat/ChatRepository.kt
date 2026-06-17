package com.hamon.kmp_pocketbase.demo.chat

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeEvent
import kotlinx.coroutines.flow.Flow

internal interface ChatRepository {
    fun subscribeToMessages(): Flow<PocketBaseResult<RealtimeEvent<Message>>>
    suspend fun sendMessage(text: String, author: String): PocketBaseResult<RecordModel<Message>>
    suspend fun getRecentMessages(): PocketBaseResult<List<RecordModel<Message>>>
}
