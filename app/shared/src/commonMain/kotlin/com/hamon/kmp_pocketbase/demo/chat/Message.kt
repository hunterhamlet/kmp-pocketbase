package com.hamon.kmp_pocketbase.demo.chat

import kotlinx.serialization.Serializable

@Serializable
internal data class Message(
    val text: String = "",
    val author: String = "",
)
