package dev.flyingpigs.composedemo.feature.login.data

import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.login.data.remote.AuthApi
import dev.flyingpigs.composedemo.feature.login.domain.LoginRepository
import dev.flyingpigs.composedemo.feature.login.domain.model.AuthSession

/**
 * Default [LoginRepository] implementation. Two jobs:
 *  1. catch failures here so they become a [DataResult.Failure] instead of an
 *     exception thrown at the UI, and
 *  2. map the API DTO down to the domain [AuthSession].
 *
 * [AuthApi] is injected by Koin — this class no longer constructs its own deps.
 */
class LoginRepositoryImpl(
    private val api: AuthApi,
) : LoginRepository {
    override suspend fun login(username: String, password: String): DataResult<AuthSession> =
        try {
            val dto = api.login(username, password)
            DataResult.Success(
                AuthSession(token = dto.accessToken, username = dto.username),
            )
        } catch (e: Exception) {
            DataResult.Failure(e.message ?: "Login failed")
        }
}
