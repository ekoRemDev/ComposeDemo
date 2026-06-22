package dev.flyingpigs.composedemo.feature.login.domain

import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.login.domain.model.AuthSession

/**
 * The contract the presentation layer depends on. It lives in `domain/` (the
 * layer that knows nothing about Ktor/JSON) and returns a [DataResult] of a
 * domain [AuthSession] — never a DTO, never a raw exception.
 *
 * An interface so the ViewModel can be tested with a fake and so DI can provide
 * the implementation.
 */
interface LoginRepository {
    suspend fun login(username: String, password: String): DataResult<AuthSession>
}
