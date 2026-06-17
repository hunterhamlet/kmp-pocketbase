package com.hamon.kmp_pocketbase.demo

import android.util.Log
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseLogger

internal actual fun createDemoLogger(): PocketBaseLogger = PocketBaseLogger { Log.d("PocketBase", it) }
