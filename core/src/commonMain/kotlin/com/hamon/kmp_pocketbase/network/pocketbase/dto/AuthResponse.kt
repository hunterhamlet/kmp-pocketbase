package com.hamon.kmp_pocketbase.network.pocketbase.dto

data class AuthResponse<T>(
    val token: String,
    val record: RecordModel<T>,
)
