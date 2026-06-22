package dev.flyingpigs.composedemo.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds the Ktor [HttpClient] shared across features.
 *
 * Lives in `core/` because it's cross-feature infrastructure, not login-specific.
 * Centralising it here is also the seam where a DI container will later provide a
 * single client instance to every feature instead of each one creating its own.
 */
fun createHttpClient(): HttpClient = HttpClient {
    // ContentNegotiation = serialize request bodies / parse responses as JSON
    // automatically, so call sites never touch Json.encode/decode by hand.
    install(ContentNegotiation) {
        json(
            Json {
                // Don't crash when the server sends fields we didn't model.
                ignoreUnknownKeys = true
            }
        )
    }
}
