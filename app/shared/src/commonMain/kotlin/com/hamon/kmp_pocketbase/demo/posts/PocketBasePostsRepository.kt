package com.hamon.kmp_pocketbase.demo.posts

import com.hamon.kmp_pocketbase.generated.PocketbaseCollection
import com.hamon.kmp_pocketbase.generated.PostsRecord
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBase
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class PocketBasePostsRepository(
    private val pb: PocketBase,
) : PostsRepository {
    private val service = pb.collection(PocketbaseCollection.posts)

    override suspend fun getPosts(): PocketBaseResult<List<RecordModel<PostsRecord>>> = service.tryGetFullList()

    override suspend fun createPost(
        title: String,
        content: String,
    ): PocketBaseResult<RecordModel<PostsRecord>> =
        service.tryCreate(
            buildJsonObject {
                put("title", title)
                put("content", content)
                put("author", pb.authStore.model?.id ?: "")
            },
        )

    override suspend fun updatePost(
        id: String,
        title: String,
        content: String,
    ): PocketBaseResult<RecordModel<PostsRecord>> =
        service.tryUpdate(
            id,
            buildJsonObject {
                put("title", title)
                put("content", content)
                put("author", pb.authStore.model?.id ?: "")
            },
        )

    override suspend fun deletePost(id: String): PocketBaseResult<Unit> = service.tryDelete(id)
}
