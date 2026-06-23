package dev.flyingpigs.composedemo.feature.profile.presentation

import dev.flyingpigs.composedemo.feature.profile.domain.model.UserProfile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String?) : ProfileUiState
}