package dev.flyingpigs.composedemo.feature.login.presentation

import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.login.domain.LoginRepository
import dev.flyingpigs.composedemo.feature.login.domain.model.AuthSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A hand-written fake of the repository INTERFACE — no network, fully controlled.
 * This is the payoff of depending on `LoginRepository` instead of the concrete
 * impl: the ViewModel can be tested in complete isolation.
 */
private class FakeLoginRepository(
    private val result: DataResult<AuthSession>,
) : LoginRepository {
    var calledWith: Pair<String, String>? = null
    override suspend fun login(username: String, password: String): DataResult<AuthSession> {
        calledWith = username to password
        return result
    }
}

class LoginViewModelTest {

    // viewModelScope dispatches on Dispatchers.Main, which doesn't exist in a
    // plain test. setMain() installs a test dispatcher; Unconfined runs launched
    // coroutines eagerly so we can assert right after onEvent().
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsIdle() {
        val vm = LoginViewModel(FakeLoginRepository(DataResult.Success(AuthSession("t", "u"))))
        assertEquals(LoginUiState.Idle, vm.uiState)
    }

    @Test
    fun typingUpdatesFields() {
        val vm = LoginViewModel(FakeLoginRepository(DataResult.Success(AuthSession("t", "u"))))
        vm.onEvent(LoginEvent.UsernameChanged("neo"))
        vm.onEvent(LoginEvent.PasswordChanged("matrix"))
        assertEquals("neo", vm.username)
        assertEquals("matrix", vm.password)
    }

    @Test
    fun submitSuccess_setsSuccessWithToken() = runTest {
        val repo = FakeLoginRepository(DataResult.Success(AuthSession(token = "abc123", username = "emilys")))
        val vm = LoginViewModel(repo)

        vm.onEvent(LoginEvent.Submit)

        val state = vm.uiState
        assertTrue(state is LoginUiState.Success, "expected Success but was $state")
        assertEquals("abc123", state.token)
        assertEquals("emilys" to "emilyspass", repo.calledWith) // uses the default prefilled creds
    }

    @Test
    fun submitFailure_setsErrorWithMessage() = runTest {
        val vm = LoginViewModel(FakeLoginRepository(DataResult.Failure("bad credentials")))

        vm.onEvent(LoginEvent.Submit)

        val state = vm.uiState
        assertTrue(state is LoginUiState.Error, "expected Error but was $state")
        assertEquals("bad credentials", state.message)
    }
}
