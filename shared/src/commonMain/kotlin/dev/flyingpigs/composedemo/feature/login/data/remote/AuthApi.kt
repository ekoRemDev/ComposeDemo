package dev.flyingpigs.composedemo.feature.login.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Remote data source for the auth API (https://dummyjson.com/auth).
 * Test credentials: username "emilys", password "emilyspass".
 *
 * The lowest layer: it knows Ktor + the JSON DTOs, and nothing above it does.
 * The [HttpClient] is injected (DI provides the one shared instance).
 */
class AuthApi(
    private val client: HttpClient,
) {
    /** Suspends during the request; throws on non-2xx (e.g. bad creds → 400). */
    suspend fun login(username: String, password: String): LoginResponse {
        val response = client.post("https://dummyjson.com/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = username, password = password))
        }
        return response.body()
    }
}
