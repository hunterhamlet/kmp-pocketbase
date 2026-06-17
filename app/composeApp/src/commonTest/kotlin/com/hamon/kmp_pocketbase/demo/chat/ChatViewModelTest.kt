package com.hamon.kmp_pocketbase.demo.chat

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeAction
import com.hamon.kmp_pocketbase.network.pocketbase.realtime.RealtimeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun message(id: String = "1") = RecordModel(id = id, fields = Message(text = "Hi", author = "User"))

    private fun viewModel(repo: ChatRepository = FakeChatRepository()) = ChatViewModel(repo)

    @Test
    fun initialStateLoadsMessagesSuccessfully() = runTest {
        val vm = viewModel(FakeChatRepository(messages = listOf(message())))
        assertIs<ChatUiState.Ready>(vm.uiState)
    }

    @Test
    fun initialStateIsErrorWhenLoadFails() = runTest {
        val vm = viewModel(FakeChatRepository(getResult = PocketBaseResult.Failure(RuntimeException("err"))))
        assertIs<ChatUiState.Error>(vm.uiState)
    }

    @Test
    fun sendMessageClearsInputOnSuccess() = runTest {
        val vm = viewModel()
        vm.messageInput = "Hello"
        vm.authorInput = "User"
        vm.sendMessage()
        assertEquals("", vm.messageInput)
    }

    @Test
    fun sendMessageDoesNothingWhenInputIsBlank() = runTest {
        val vm = viewModel(FakeChatRepository(sendResult = PocketBaseResult.Failure(RuntimeException("err"))))
        vm.messageInput = ""
        vm.authorInput = "User"
        vm.sendMessage()
        assertEquals("", vm.messageInput)
    }

    @Test
    fun sendMessageDoesNothingWhenAuthorIsBlank() = runTest {
        val vm = viewModel()
        vm.messageInput = "Hello"
        vm.authorInput = ""
        vm.sendMessage()
        assertEquals("Hello", vm.messageInput)
    }
}

private class FakeChatRepository(
    messages: List<RecordModel<Message>> = emptyList(),
    getResult: PocketBaseResult<List<RecordModel<Message>>> = PocketBaseResult.Success(messages),
    private val sendResult: PocketBaseResult<RecordModel<Message>> = PocketBaseResult.Success(RecordModel(fields = Message())),
    private val events: Flow<PocketBaseResult<RealtimeEvent<Message>>> = emptyFlow(),
) : ChatRepository {
    private val _getResult = getResult

    override fun subscribeToMessages() = events
    override suspend fun sendMessage(text: String, author: String) = sendResult
    override suspend fun getRecentMessages() = _getResult
}
