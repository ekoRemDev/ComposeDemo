package dev.flyingpigs.composedemo.feature.login.data

import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.login.data.remote.AuthApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests the repository + AuthApi together against a FAKE HTTP engine.
 * MockEngine lets us return any canned response without touching the network,
 * so we can verify: (1) JSON → DTO → domain mapping, (2) errors become
 * DataResult.Failure instead of crashing.
 */
class LoginRepositoryImplTest {

    /** Builds a repository whose HTTP layer returns exactly [body] with [status]. */
    private fun repositoryReturning(status: HttpStatusCode, body: String): LoginRepositoryImpl {
        val engine = MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return LoginRepositoryImpl(AuthApi(client))
    }

    @Test
    fun success_mapsDtoToDomain() = runTest {
        val json = """
            {
              "id": 1, "username": "emilys", "email": "e@x.com",
              "firstName": "Emily", "lastName": "Stone",
              "accessToken": "tok_123", "refreshToken": "ref_456"
            }
        """.trimIndent()
        val repo = repositoryReturning(HttpStatusCode.OK, json)

        val result = repo.login("emilys", "emilyspass")

        assertTrue(result is DataResult.Success, "expected Success but was $result")
        assertEquals("tok_123", result.data.token)        // accessToken → token
        assertEquals("emilys", result.data.username)
    }

    @Test
    fun httpError_becomesFailure() = runTest {
        val repo = repositoryReturning(HttpStatusCode.BadRequest, """{"message":"Invalid"}""")

        val result = repo.login("emilys", "wrong")

        assertTrue(result is DataResult.Failure, "expected Failure but was $result")
    }
}
