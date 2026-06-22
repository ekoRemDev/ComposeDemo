package dev.flyingpigs.composedemo.feature.login.domain.model

/**
 * Domain model: what a logged-in session means to the app, independent of how
 * the server happens to shape its JSON. The data layer maps the API DTO into
 * this, so nothing above the repository ever sees `LoginResponse`.
 */
data class AuthSession(
    val token: String,
    val username: String,
)
