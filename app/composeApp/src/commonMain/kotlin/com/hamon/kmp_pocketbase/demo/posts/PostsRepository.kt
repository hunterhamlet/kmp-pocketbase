package com.hamon.kmp_pocketbase.demo.posts

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

internal interface PostsRepository {
    suspend fun getPosts(): PocketBaseResult<List<RecordModel<Post>>>
    suspend fun createPost(title: String, content: String): PocketBaseResult<RecordModel<Post>>
    suspend fun updatePost(id: String, title: String, content: String): PocketBaseResult<RecordModel<Post>>
    suspend fun deletePost(id: String): PocketBaseResult<Unit>
}
