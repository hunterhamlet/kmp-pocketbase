package com.hamon.kmp_pocketbase.demo.posts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamon.kmp_pocketbase.generated.PostsRecord
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlinx.coroutines.launch

internal sealed class PostsUiState {
    object Loading : PostsUiState()

    data class Success(
        val posts: List<RecordModel<PostsRecord>>,
    ) : PostsUiState()

    data class Error(
        val message: String,
    ) : PostsUiState()
}

internal sealed class PostActionState {
    object Idle : PostActionState()

    object Loading : PostActionState()

    object Done : PostActionState()

    data class Error(
        val message: String,
    ) : PostActionState()
}

internal class PostsViewModel(
    private val repo: PostsRepository,
) : ViewModel() {
    var postsState by mutableStateOf<PostsUiState>(PostsUiState.Loading)
        private set
    var actionState by mutableStateOf<PostActionState>(PostActionState.Idle)
        private set

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            postsState = PostsUiState.Loading
            repo
                .getPosts()
                .onSuccess { postsState = PostsUiState.Success(it) }
                .onFailure { postsState = PostsUiState.Error(it.message ?: "Failed to load posts") }
        }
    }

    fun createPost(
        title: String,
        content: String,
    ) {
        viewModelScope.launch {
            actionState = PostActionState.Loading
            repo
                .createPost(title, content)
                .onSuccess { newRecord ->
                    val current = (postsState as? PostsUiState.Success)?.posts ?: emptyList()
                    postsState = PostsUiState.Success(current + newRecord)
                    actionState = PostActionState.Done
                }.onFailure { actionState = PostActionState.Error(it.message ?: "Failed to create post") }
        }
    }

    fun updatePost(
        id: String,
        title: String,
        content: String,
    ) {
        viewModelScope.launch {
            actionState = PostActionState.Loading
            repo
                .updatePost(id, title, content)
                .onSuccess { updated ->
                    val current =
                        (postsState as? PostsUiState.Success)?.posts?.toMutableList() ?: mutableListOf()
                    val idx = current.indexOfFirst { it.id == id }
                    if (idx >= 0) current[idx] = updated
                    postsState = PostsUiState.Success(current.toList())
                    actionState = PostActionState.Done
                }.onFailure { actionState = PostActionState.Error(it.message ?: "Failed to update post") }
        }
    }

    fun deletePost(id: String) {
        viewModelScope.launch {
            actionState = PostActionState.Loading
            repo
                .deletePost(id)
                .onSuccess {
                    val current =
                        (postsState as? PostsUiState.Success)?.posts?.filter { it.id != id } ?: emptyList()
                    postsState = PostsUiState.Success(current)
                    actionState = PostActionState.Done
                }.onFailure { actionState = PostActionState.Error(it.message ?: "Failed to delete post") }
        }
    }

    fun resetActionState() {
        actionState = PostActionState.Idle
    }
}
