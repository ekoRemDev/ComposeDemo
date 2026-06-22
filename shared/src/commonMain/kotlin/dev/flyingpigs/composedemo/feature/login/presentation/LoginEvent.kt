package dev.flyingpigs.composedemo.feature.login.presentation

/**
 * User intents the screen can send to the ViewModel — the "events flow up" half
 * of MVI. The screen never mutates state or calls business logic directly; it
 * just reports what the user did via a single `onEvent(event)` entry point, and
 * the ViewModel decides what that means.
 *
 * (Flutter analogy: these are like Bloc events you `add()` to a Bloc.)
 */
sealed interface LoginEvent {
    data class UsernameChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object Submit : LoginEvent
}
