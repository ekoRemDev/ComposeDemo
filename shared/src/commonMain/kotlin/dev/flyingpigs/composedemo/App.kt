package dev.flyingpigs.composedemo

import dev.flyingpigs.composedemo.ui.components.AppBottomBar
import dev.flyingpigs.composedemo.ui.components.AppFloatingActionButton
import dev.flyingpigs.composedemo.ui.components.AppTopBar
import dev.flyingpigs.composedemo.ui.screens.FavoritesScreen
import dev.flyingpigs.composedemo.ui.screens.HomeScreen
import dev.flyingpigs.composedemo.ui.screens.ProfileScreen
import dev.flyingpigs.composedemo.ui.screens.SearchScreen
import dev.flyingpigs.composedemo.ui.screens.SettingsScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedTab by remember { mutableStateOf(0) }
        Scaffold(
            topBar = { AppTopBar() },
            bottomBar = {
                AppBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
            containerColor = Color(red = 187, green = 134, blue = 252),
            floatingActionButton = { AppFloatingActionButton() },
            content = { innerPadding ->
                Surface(
                    color = Color(red = 187, green = 134, blue = 252),
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen()
                        1 -> SearchScreen()
                        2 -> FavoritesScreen()
                        3 -> ProfileScreen()
                        4 -> SettingsScreen()
                    }
                }
            },
        )
    }
}
