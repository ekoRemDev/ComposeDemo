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
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.flyingpigs.composedemo.core.navigation.FavoritesTab
import dev.flyingpigs.composedemo.core.navigation.HomeTab
import dev.flyingpigs.composedemo.core.navigation.ProfileTab
import dev.flyingpigs.composedemo.core.navigation.SearchTab
import dev.flyingpigs.composedemo.core.navigation.SettingsTab

data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

val bottomTabs = listOf(
    BottomTab("Home", Icons.Default.Home, HomeTab),
    BottomTab("Search", Icons.Default.Search, SearchTab),
    BottomTab("Favorites", Icons.Default.Favorite, FavoritesTab),
    BottomTab("Profile", Icons.Default.Person, ProfileTab),
    BottomTab("Settings", Icons.Default.Settings, SettingsTab),
)

/** Index of the tab a destination belongs to, or -1 if it's not in any tab. */
fun tabIndexOf(destination: NavDestination?): Int =
    bottomTabs.indexOfFirst { tab ->
        destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    }

@Composable
fun AppBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy
                    ?.any { it.hasRoute(tab.route::class) } == true,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}
