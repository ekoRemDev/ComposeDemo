package dev.flyingpigs.composedemo.feature.login.data.remote

import kotlinx.serialization.Serializable

/**
 * Wire-format DTOs for the auth API — an internal detail of the remote data
 * source. They never leave the data layer; the repository maps them to the
 * domain model.
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val expiresInMins: Int = 30,
)

@Serializable
data class LoginResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val accessToken: String,
    val refreshToken: String,
)
