package com.hamon.kmp_pocketbase.network.pocketbase.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val record: RecordModel,
)
