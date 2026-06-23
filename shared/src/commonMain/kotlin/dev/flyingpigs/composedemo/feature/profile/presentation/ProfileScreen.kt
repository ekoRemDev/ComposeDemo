package dev.flyingpigs.composedemo.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    // One-shot: trigger the load once when the screen first appears.
    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.Load)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Render based on the current state — exhaustive `when`.
        when (val s = viewModel.uiState) {
            ProfileUiState.Loading -> CircularProgressIndicator()

            is ProfileUiState.Success -> {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
                Text(s.profile.name, style = MaterialTheme.typography.headlineMedium)
                Text(
                    s.profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            is ProfileUiState.Error -> {
                Text(
                    s.message ?: "Something went wrong",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
