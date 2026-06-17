package com.hamon.kmp_pocketbase.demo.auth

import kotlinx.serialization.Serializable

@Serializable
internal data class UserRecord(
    val email: String = "",
    val name: String = "",
)
