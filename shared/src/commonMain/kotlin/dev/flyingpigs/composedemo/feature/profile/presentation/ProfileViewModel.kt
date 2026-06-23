package dev.flyingpigs.composedemo.feature.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.flyingpigs.composedemo.core.util.DataResult
import dev.flyingpigs.composedemo.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: ProfileRepository) : ViewModel() {
    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading); private set
    fun onEvent(e: ProfileEvent) {
        when (e) {
            ProfileEvent.Load, ProfileEvent.ReTry -> load()
        }
    }

    private fun load() {
        uiState = ProfileUiState.Loading
        viewModelScope.launch {
            uiState = when (val r = repo.getUserProfile()) {
                is DataResult.Success -> ProfileUiState.Success(r.data)
                is DataResult.Failure -> ProfileUiState.Error(r.message)
            }
        }
    }
}