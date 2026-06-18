package dev.flyingpigs.composedemo.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FavoriteDetailScreen(name: String) {
    Text(text = "Favorite Details $name", style = MaterialTheme.typography.headlineMedium)
}