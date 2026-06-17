package com.hamon.kmp_pocketbase.demo.auth

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.AuthResponse
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

internal interface AuthRepository {
    val isLoggedIn: Boolean
    suspend fun login(email: String, password: String): PocketBaseResult<AuthResponse<UserRecord>>
    suspend fun register(email: String, password: String, name: String): PocketBaseResult<RecordModel<UserRecord>>
    suspend fun refresh(): PocketBaseResult<AuthResponse<UserRecord>>
    suspend fun logout()
}
