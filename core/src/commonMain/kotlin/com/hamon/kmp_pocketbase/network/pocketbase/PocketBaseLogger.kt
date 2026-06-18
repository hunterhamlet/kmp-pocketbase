package com.hamon.kmp_pocketbase.network.pocketbase

fun interface PocketBaseLogger {
    fun log(message: String)

    companion object {
        val Default: PocketBaseLogger = PocketBaseLogger { message -> println(message) }
    }
}
