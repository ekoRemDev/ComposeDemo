package dev.flyingpigs.composedemo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomTab(
    val label: String,
    val icon: ImageVector,
)

val bottomTabs = listOf(
    BottomTab("Home", Icons.Default.Home),
    BottomTab("Search", Icons.Default.Search),
    BottomTab("Favorites", Icons.Default.Favorite),
    BottomTab("Profile", Icons.Default.Person),
    BottomTab("Settings", Icons.Default.Settings),
)

@Composable
fun AppBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar {
        bottomTabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}
