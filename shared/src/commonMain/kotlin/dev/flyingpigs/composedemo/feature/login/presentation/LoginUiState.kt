package dev.flyingpigs.composedemo.feature.login.presentation

/**
 * The four states the login screen can be in. A sealed interface makes the
 * `when` over it exhaustive — the compiler forces the UI to handle every case.
 */
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val token: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
