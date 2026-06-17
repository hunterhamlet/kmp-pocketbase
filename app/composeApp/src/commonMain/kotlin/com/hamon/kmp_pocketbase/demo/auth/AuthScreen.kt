package com.hamon.kmp_pocketbase.demo.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hamon.kmp_pocketbase.demo.PocketBaseProvider

@Composable
internal fun AuthScreen(onAuthenticated: () -> Unit) {
    val vm = viewModel { AuthViewModel(PocketBaseAuthRepository(PocketBaseProvider.instance)) }
    var tabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(vm.uiState) {
        if (vm.uiState is AuthUiState.Success) {
            onAuthenticated()
            vm.resetState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("KMP PocketBase Demo", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        PrimaryTabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Login") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Register") })
        }

        Spacer(Modifier.height(16.dp))

        when (tabIndex) {
            0 -> LoginForm(vm)
            1 -> RegisterForm(vm)
        }

        val state = vm.uiState
        if (state is AuthUiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LoginForm(vm: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = vm.email,
            onValueChange = { vm.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        if (vm.uiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = vm::login, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }
            TextButton(onClick = vm::refresh) {
                Text("Refresh token")
            }
        }
    }
}

@Composable
private fun RegisterForm(vm: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = vm.name,
            onValueChange = { vm.name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = vm.email,
            onValueChange = { vm.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        if (vm.uiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = vm::register, modifier = Modifier.fillMaxWidth()) {
                Text("Register")
            }
        }
    }
}
