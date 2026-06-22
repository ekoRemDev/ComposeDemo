package dev.flyingpigs.composedemo.feature.login.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.login.domain.LoginRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    // Depends on the domain interface; Koin injects the implementation.
    private val repository: LoginRepository,
) : ViewModel() {

    var username by mutableStateOf("emilys")        // pre-filled test creds
        private set
    var password by mutableStateOf("emilyspass")
        private set
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    /** Single entry point for everything the user does on the screen. */
    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> username = event.value
            is LoginEvent.PasswordChanged -> password = event.value
            LoginEvent.Submit -> login()
        }
    }

    private fun login() {
        if (uiState is LoginUiState.Loading) return   // ignore double taps

        uiState = LoginUiState.Loading
        viewModelScope.launch {
            // The repository returns a DataResult — no try/catch here, and the
            // UI never sees a raw exception or a DTO.
            uiState = when (val result = repository.login(username, password)) {
                is DataResult.Success -> LoginUiState.Success(result.data.token)
                is DataResult.Failure -> LoginUiState.Error(result.message)
            }
        }
    }
}
