package com.hamon.kmp_pocketbase.demo.posts

import kotlinx.serialization.Serializable

@Serializable
internal data class Post(
    val title: String = "",
    val content: String = "",
    val author: String = "",
)
