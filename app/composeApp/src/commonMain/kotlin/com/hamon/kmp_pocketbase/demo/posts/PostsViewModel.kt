package com.hamon.kmp_pocketbase.demo.posts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlinx.coroutines.launch

internal sealed class PostsUiState {
    object Loading : PostsUiState()
    data class Success(val posts: List<RecordModel<Post>>) : PostsUiState()
    data class Error(val message: String) : PostsUiState()
}

internal sealed class PostActionState {
    object Idle : PostActionState()
    object Loading : PostActionState()
    object Done : PostActionState()
    data class Error(val message: String) : PostActionState()
}

internal class PostsViewModel(private val repo: PostsRepository) : ViewModel() {
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
            repo.getPosts()
                .onSuccess { postsState = PostsUiState.Success(it) }
                .onFailure { postsState = PostsUiState.Error(it.message ?: "Failed to load posts") }
        }
    }

    fun createPost(title: String, content: String) {
        viewModelScope.launch {
            actionState = PostActionState.Loading
            repo.createPost(title, content)
                .onSuccess {
                    actionState = PostActionState.Done
                    loadPosts()
                }
                .onFailure { actionState = PostActionState.Error(it.message ?: "Failed to create post") }
        }
    }

    fun updatePost(id: String, title: String, content: String) {
        viewModelScope.launch {
            actionState = PostActionState.Loading
            repo.updatePost(id, title, content)
                .onSuccess {
                    actionState = PostActionState.Done
                    loadPosts()
                }
                .onFailure { actionState = PostActionState.Error(it.message ?: "Failed to update post") }
        }
    }

    fun deletePost(id: String) {
        viewModelScope.launch {
            actionState = PostActionState.Loading
            repo.deletePost(id)
                .onSuccess {
                    actionState = PostActionState.Done
                    loadPosts()
                }
                .onFailure { actionState = PostActionState.Error(it.message ?: "Failed to delete post") }
        }
    }

    fun resetActionState() {
        actionState = PostActionState.Idle
    }
}
