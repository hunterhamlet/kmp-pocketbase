package com.hamon.kmp_pocketbase

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
