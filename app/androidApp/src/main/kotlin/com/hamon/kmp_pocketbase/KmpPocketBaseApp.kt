package com.hamon.kmp_pocketbase

import android.app.Application
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBase
import com.hamon.kmp_pocketbase.network.pocketbase.init

class KmpPocketBaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PocketBase.init(this)
    }
}
