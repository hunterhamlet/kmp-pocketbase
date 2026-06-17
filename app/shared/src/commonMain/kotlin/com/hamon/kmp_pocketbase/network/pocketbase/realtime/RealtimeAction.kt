package com.hamon.kmp_pocketbase.network.pocketbase.realtime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RealtimeAction {
    @SerialName("create")
    CREATE,

    @SerialName("update")
    UPDATE,

    @SerialName("delete")
    DELETE,
}
