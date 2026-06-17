package com.hamon.kmp_pocketbase.network.pocketbase

import android.content.Context

internal object PocketBaseAndroidContext {
    var appContext: Context? = null
}

fun PocketBase.Companion.init(context: Context) {
    PocketBaseAndroidContext.appContext = context.applicationContext
}
