package com.hamon.kmp_pocketbase.demo

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import com.hamon.kmp_pocketbase.demo.posts.Post

internal sealed class Screen {
    object Auth : Screen()
    object Posts : Screen()
    object CreatePost : Screen()
    data class EditPost(val post: RecordModel<Post>) : Screen()
    object Chat : Screen()
}
