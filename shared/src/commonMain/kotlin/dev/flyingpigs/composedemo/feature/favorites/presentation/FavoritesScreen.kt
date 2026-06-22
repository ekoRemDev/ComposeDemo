package dev.flyingpigs.composedemo.feature.favorites.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesScreen(onFavoriteClick: (String) -> Unit) {
    val favorites = listOf("Kotlin", "Compose Multiplatform", "Material 3", "Gradle", "Coroutines")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        items(favorites) { favorite ->
            ListItem(
                modifier = Modifier.clickable { onFavoriteClick(favorite)},
                headlineContent = { Text(favorite) },
                leadingContent = {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                },
            )
        }
    }
}
