package com.hamon.kmp_pocketbase.demo.auth

import com.hamon.kmp_pocketbase.generated.UsersRecord
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.AuthResponse
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repo: AuthRepository = FakeAuthRepository()) = AuthViewModel(repo)

    @Test
    fun initialStateIsIdle() {
        assertIs<AuthUiState.Idle>(viewModel().uiState)
    }

    @Test
    fun loginSuccessTransitionsToSuccess() =
        runTest {
            val vm = viewModel(FakeAuthRepository(loginResult = PocketBaseResult.Success(fakeAuthResponse())))
            vm.email = "user@test.com"
            vm.password = "password"
            vm.login()
            assertIs<AuthUiState.Success>(vm.uiState)
        }

    @Test
    fun loginFailureTransitionsToError() =
        runTest {
            val vm = viewModel(FakeAuthRepository(loginResult = PocketBaseResult.Failure(RuntimeException("401"))))
            vm.login()
            assertIs<AuthUiState.Error>(vm.uiState)
        }

    @Test
    fun registerSuccessTransitionsToSuccess() =
        runTest {
            val vm = viewModel(FakeAuthRepository(registerResult = PocketBaseResult.Success(fakeUserRecord())))
            vm.email = "user@test.com"
            vm.password = "password"
            vm.name = "User"
            vm.register()
            assertIs<AuthUiState.Success>(vm.uiState)
        }

    @Test
    fun registerFailureTransitionsToError() =
        runTest {
            val vm = viewModel(FakeAuthRepository(registerResult = PocketBaseResult.Failure(RuntimeException("400"))))
            vm.register()
            assertIs<AuthUiState.Error>(vm.uiState)
        }

    @Test
    fun refreshSuccessTransitionsToSuccess() =
        runTest {
            val vm = viewModel(FakeAuthRepository(refreshResult = PocketBaseResult.Success(fakeAuthResponse())))
            vm.refresh()
            assertIs<AuthUiState.Success>(vm.uiState)
        }

    @Test
    fun logoutResetsStateToIdle() =
        runTest {
            val vm = viewModel()
            vm.logout()
            assertIs<AuthUiState.Idle>(vm.uiState)
        }

    @Test
    fun resetStateSetsStateToIdle() =
        runTest {
            val vm = viewModel(FakeAuthRepository(loginResult = PocketBaseResult.Failure(RuntimeException("err"))))
            vm.login()
            assertIs<AuthUiState.Error>(vm.uiState)
            vm.resetState()
            assertIs<AuthUiState.Idle>(vm.uiState)
        }

    private fun fakeUserRecord() =
        RecordModel(
            id = "1",
            fields = UsersRecord(id = "1", email = "user@test.com", name = "User"),
        )

    private fun fakeAuthResponse() = AuthResponse(token = "tok", record = fakeUserRecord())
}

private class FakeAuthRepository(
    private val loginResult: PocketBaseResult<AuthResponse<UsersRecord>> =
        PocketBaseResult.Success(
            AuthResponse("tok", RecordModel(fields = UsersRecord(id = "", email = ""))),
        ),
    private val registerResult: PocketBaseResult<RecordModel<UsersRecord>> =
        PocketBaseResult.Success(RecordModel(fields = UsersRecord(id = "", email = ""))),
    private val refreshResult: PocketBaseResult<AuthResponse<UsersRecord>> =
        PocketBaseResult.Success(
            AuthResponse("tok", RecordModel(fields = UsersRecord(id = "", email = ""))),
        ),
) : AuthRepository {
    override var isLoggedIn = false
        private set

    override suspend fun login(
        email: String,
        password: String,
    ) = loginResult

    override suspend fun register(
        email: String,
        password: String,
        name: String,
    ) = registerResult

    override suspend fun refresh() = refreshResult

    override suspend fun logout() {
        isLoggedIn = false
    }
}
