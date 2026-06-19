package com.hamon.kmp_pocketbase.demo.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import com.hamon.kmp_pocketbase.generated.PostsRecord
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostsListScreen(
    onCreatePost: () -> Unit,
    onEditPost: (RecordModel<PostsRecord>) -> Unit,
    onChat: () -> Unit,
    onLogout: () -> Unit,
) {
    val pb = PocketBaseProvider.instance
    val vm = viewModel { PostsViewModel(PocketBasePostsRepository(pb)) }

    LaunchedEffect(Unit) { vm.loadPosts() }
    LaunchedEffect(vm.actionState) {
        if (vm.actionState is PostActionState.Done) vm.resetActionState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posts") },
                actions = {
                    TextButton(onClick = onChat) { Text("Chat") }
                    TextButton(onClick = onLogout) { Text("Logout") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) {
                Text("+")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = vm.postsState) {
                is PostsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is PostsUiState.Error ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = vm::loadPosts) { Text("Retry") }
                    }
                is PostsUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        Text(
                            "No posts yet. Create one!",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.posts, key = { it.id }) { record ->
                                PostCard(
                                    record = record,
                                    onEdit = { onEditPost(record) },
                                    onDelete = { vm.deletePost(record.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    record: RecordModel<PostsRecord>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(record.fields.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(record.fields.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
