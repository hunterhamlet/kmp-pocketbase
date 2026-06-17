package com.hamon.kmp_pocketbase.demo.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hamon.kmp_pocketbase.demo.PocketBaseProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(onBack: () -> Unit) {
    val pb = PocketBaseProvider.instance
    val vm = viewModel { ChatViewModel(PocketBaseChatRepository(pb)) }
    val listState = rememberLazyListState()

    val state = vm.uiState
    LaunchedEffect(state) {
        if (state is ChatUiState.Ready && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (state) {
                    is ChatUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is ChatUiState.Error -> Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    is ChatUiState.Ready -> {
                        if (state.messages.isEmpty()) {
                            Text("No messages yet.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(state.messages, key = { it.id }) { record ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                record.fields.author,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(record.fields.text)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vm.authorInput,
                    onValueChange = { vm.authorInput = it },
                    label = { Text("Your name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = vm.messageInput,
                        onValueChange = { vm.messageInput = it },
                        label = { Text("Message") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(onClick = vm::sendMessage) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
