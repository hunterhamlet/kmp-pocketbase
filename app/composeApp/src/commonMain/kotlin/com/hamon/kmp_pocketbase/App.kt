package com.hamon.kmp_pocketbase

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.hamon.kmp_pocketbase.demo.PocketBaseProvider
import com.hamon.kmp_pocketbase.demo.Screen
import com.hamon.kmp_pocketbase.demo.auth.AuthScreen
import com.hamon.kmp_pocketbase.demo.chat.ChatScreen
import com.hamon.kmp_pocketbase.demo.posts.PostFormScreen
import com.hamon.kmp_pocketbase.demo.posts.PostsListScreen
import kotlinx.coroutines.launch

@Composable
fun App() {
    val pb = PocketBaseProvider.instance
    val scope = rememberCoroutineScope()
    var currentScreen by remember {
        mutableStateOf<Screen>(if (pb.authStore.isValid) Screen.Posts else Screen.Auth)
    }

    MaterialTheme {
        when (val screen = currentScreen) {
            Screen.Auth -> AuthScreen(
                onAuthenticated = { currentScreen = Screen.Posts },
            )
            Screen.Posts -> PostsListScreen(
                onCreatePost = { currentScreen = Screen.CreatePost },
                onEditPost = { currentScreen = Screen.EditPost(it) },
                onChat = { currentScreen = Screen.Chat },
                onLogout = {
                    scope.launch { pb.authStore.clear() }
                    currentScreen = Screen.Auth
                },
            )
            Screen.CreatePost -> PostFormScreen(
                post = null,
                onSaved = { currentScreen = Screen.Posts },
                onBack = { currentScreen = Screen.Posts },
            )
            is Screen.EditPost -> PostFormScreen(
                post = screen.post,
                onSaved = { currentScreen = Screen.Posts },
                onBack = { currentScreen = Screen.Posts },
            )
            Screen.Chat -> ChatScreen(
                onBack = { currentScreen = Screen.Posts },
            )
        }
    }
}
