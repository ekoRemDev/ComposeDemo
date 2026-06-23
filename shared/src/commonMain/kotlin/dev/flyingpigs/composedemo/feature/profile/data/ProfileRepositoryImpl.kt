package dev.flyingpigs.composedemo.feature.profile.data

import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.profile.data.remote.ProfileApi
import dev.flyingpigs.composedemo.feature.profile.domain.ProfileRepository
import dev.flyingpigs.composedemo.feature.profile.domain.model.UserProfile

class ProfileRepositoryImpl(private val api: ProfileApi) : ProfileRepository {
    override suspend fun getUserProfile(): DataResult<UserProfile> = try {
        val dto = api.fetch()
        DataResult.Success(UserProfile(name = dto.firstName, email = dto.email))
    } catch (e: Exception) {
        DataResult.Failure(e.message ?: "Unknown error")
    }
}