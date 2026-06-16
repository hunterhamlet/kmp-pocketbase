package com.hamon.kmp_pocketbase.network.pocketbase.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecordModel(
    val id: String = "",
    val collectionId: String = "",
    val collectionName: String = "",
    val created: String = "",
    val updated: String = "",
)
