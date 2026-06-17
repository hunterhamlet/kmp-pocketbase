package com.hamon.kmp_pocketbase.network.pocketbase

class PocketBaseNotInitializedException :
    Error(
        "PocketBase.init(context) must be called from Application.onCreate() before using TokenPersistence.Encrypted on Android.",
    )
