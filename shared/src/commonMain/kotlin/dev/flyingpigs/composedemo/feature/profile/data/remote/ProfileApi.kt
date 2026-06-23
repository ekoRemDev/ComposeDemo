package dev.flyingpigs.composedemo.feature.profile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ProfileApi (private val client: HttpClient){
    suspend fun fetch(): ProfileDto = client.get("https://dummyjson.com/users/1").body()
}
