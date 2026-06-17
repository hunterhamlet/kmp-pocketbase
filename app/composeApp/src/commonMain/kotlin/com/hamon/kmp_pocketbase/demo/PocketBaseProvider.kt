package com.hamon.kmp_pocketbase.demo

import com.hamon.kmp_pocketbase.network.pocketbase.PocketBase
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseLogLevel
import com.hamon.kmp_pocketbase.network.pocketbase.TokenPersistence

internal object PocketBaseProvider {
    val instance: PocketBase by lazy {
        PocketBase(
            baseUrl = AppConfig.POCKETBASE_URL,
            tokenPersistence = TokenPersistence.Encrypted,
            logLevel = PocketBaseLogLevel.BASIC,
            logger = createDemoLogger(),
        )
    }
}
