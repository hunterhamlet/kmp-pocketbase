package com.hamon.kmp_pocketbase.demo.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hamon.kmp_pocketbase.demo.PocketBaseProvider
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostFormScreen(
    post: RecordModel<Post>?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val pb = PocketBaseProvider.instance
    val vm = viewModel(key = post?.id ?: "new") { PostsViewModel(PocketBasePostsRepository(pb)) }

    var title by remember { mutableStateOf(post?.fields?.title ?: "") }
    var content by remember { mutableStateOf(post?.fields?.content ?: "") }

    LaunchedEffect(vm.actionState) {
        if (vm.actionState is PostActionState.Done) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (post == null) "New Post" else "Edit Post") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
            )
            val state = vm.actionState
            if (state is PostActionState.Error) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            if (state is PostActionState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (post == null) vm.createPost(title, content)
                        else vm.updatePost(post.id, title, content)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank(),
                ) {
                    Text("Save")
                }
            }
        }
    }
}
