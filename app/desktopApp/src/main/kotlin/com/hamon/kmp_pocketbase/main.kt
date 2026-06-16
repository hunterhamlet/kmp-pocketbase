package com.hamon.kmp_pocketbase

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmp-pocketbase",
    ) {
        App()
    }
}