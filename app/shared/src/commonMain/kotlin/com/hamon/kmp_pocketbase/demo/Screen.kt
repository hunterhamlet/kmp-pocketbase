package com.hamon.kmp_pocketbase.demo

import com.hamon.kmp_pocketbase.generated.PostsRecord
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

internal sealed class Screen {
    object Auth : Screen()

    object Posts : Screen()

    object CreatePost : Screen()

    data class EditPost(
        val post: RecordModel<PostsRecord>,
    ) : Screen()

    object Chat : Screen()
}
