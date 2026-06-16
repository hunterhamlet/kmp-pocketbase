package com.hamon.kmp_pocketbase.network.pocketbase.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResultList(
    val page: Int = 1,
    val perPage: Int = 30,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<RecordModel> = emptyList(),
)
