package com.hamon.kmp_pocketbase.network.pocketbase.realtime

import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel

data class RealtimeEvent<T>(
    val action: RealtimeAction,
    val record: RecordModel<T>,
)
