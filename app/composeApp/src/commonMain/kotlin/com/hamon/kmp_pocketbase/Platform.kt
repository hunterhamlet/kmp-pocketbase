package com.hamon.kmp_pocketbase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
