package dev.flyingpigs.composedemo.feature.profile.domain

import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.profile.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getUserProfile(): DataResult<UserProfile>
}