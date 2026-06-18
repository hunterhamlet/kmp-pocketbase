package com.hamon.kmp_pocketbase.demo.auth

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBase
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.AuthResponse
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class PocketBaseAuthRepository(
    private val pb: PocketBase,
) : AuthRepository {
    private val service = pb.collection("users")

    override val isLoggedIn: Boolean get() = pb.authStore.isValid

    override suspend fun login(
        email: String,
        password: String,
    ): PocketBaseResult<AuthResponse<UserRecord>> = service.tryAuthWithPassword(email, password)

    override suspend fun register(
        email: String,
        password: String,
        name: String,
    ): PocketBaseResult<RecordModel<UserRecord>> =
        service.tryCreate(
            buildJsonObject {
                put("email", email)
                put("password", password)
                put("passwordConfirm", password)
                put("name", name)
            },
        )

    override suspend fun refresh(): PocketBaseResult<AuthResponse<UserRecord>> = service.tryAuthRefresh()

    override suspend fun logout() {
        pb.authStore.clear()
    }
}
