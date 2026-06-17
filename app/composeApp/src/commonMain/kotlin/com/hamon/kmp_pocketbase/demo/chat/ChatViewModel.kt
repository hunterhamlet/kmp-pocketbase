package com.hamon.kmp_pocketbase.demo.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeAction
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Ready(val messages: List<RecordModel<Message>>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

internal class ChatViewModel(private val repo: ChatRepository) : ViewModel() {
    var uiState by mutableStateOf<ChatUiState>(ChatUiState.Loading)
        private set
    var messageInput by mutableStateOf("")

    init {
        loadAndSubscribe()
    }

    private fun loadAndSubscribe() {
        viewModelScope.launch {
            repo.getRecentMessages()
                .onSuccess { uiState = ChatUiState.Ready(it) }
                .onFailure { uiState = ChatUiState.Error(it.message ?: "Failed to load messages") }
        }
        repo.subscribeToMessages()
            .onEach { result ->
                result.onSuccess { event ->
                    val current = (uiState as? ChatUiState.Ready)?.messages?.toMutableList() ?: mutableListOf()
                    when (event.action) {
                        RealtimeAction.CREATE -> current.add(event.record)
                        RealtimeAction.UPDATE -> {
                            val idx = current.indexOfFirst { it.id == event.record.id }
                            if (idx >= 0) current[idx] = event.record
                        }
                        RealtimeAction.DELETE -> current.removeAll { it.id == event.record.id }
                    }
                    uiState = ChatUiState.Ready(current.toList())
                }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage() {
        val text = messageInput.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            repo.sendMessage(text)
                .onSuccess { messageInput = "" }
        }
    }
}
