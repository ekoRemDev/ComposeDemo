package dev.flyingpigs.composedemo

import dev.flyingpigs.composedemo.ui.components.AppBottomBar
import dev.flyingpigs.composedemo.ui.components.AppFloatingActionButton
import dev.flyingpigs.composedemo.ui.components.AppTopBar
import dev.flyingpigs.composedemo.ui.components.tabIndexOf
import dev.flyingpigs.composedemo.feature.favorites.presentation.FavoritesScreen
import dev.flyingpigs.composedemo.feature.home.presentation.HomeScreen
import dev.flyingpigs.composedemo.feature.profile.presentation.ProfileScreen
import dev.flyingpigs.composedemo.feature.search.presentation.SearchScreen
import dev.flyingpigs.composedemo.feature.settings.presentation.SettingsScreen

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.flyingpigs.composedemo.core.navigation.FavoriteDetail
import dev.flyingpigs.composedemo.core.navigation.Favorites
import dev.flyingpigs.composedemo.core.navigation.FavoritesTab
import dev.flyingpigs.composedemo.core.navigation.Home
import dev.flyingpigs.composedemo.core.navigation.HomeTab
import dev.flyingpigs.composedemo.core.navigation.Profile
import dev.flyingpigs.composedemo.core.navigation.ProfileTab
import dev.flyingpigs.composedemo.core.navigation.Search
import dev.flyingpigs.composedemo.core.navigation.SearchTab
import dev.flyingpigs.composedemo.core.navigation.Settings
import dev.flyingpigs.composedemo.core.navigation.SettingsTab
import dev.flyingpigs.composedemo.feature.favorites.presentation.FavoriteDetailScreen

/**
 * The main app shell: top bar, bottom navigation, FAB, and the tabbed NavHost.
 *
 * This is the "ShellRoute" in go_router terms — the bottom-bar chrome lives
 * here and only here, so the Splash/Welcome screens (above it in the root
 * NavHost) render full-screen without it. It owns its OWN navController for
 * the five tabs, separate from the root navController.
 */
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    var isVisible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { AppTopBar(onLogout = onLogout) },
        bottomBar = {
            AppBottomBar(
                navController = navController,
            )
        },
        containerColor = Color(red = 187, green = 134, blue = 252),
        floatingActionButton = {
            AppFloatingActionButton(isVisible = isVisible, onToggle = {
                isVisible = !isVisible
            })
        },
        content = { innerPadding ->
            Surface(
                color = Color(red = 187, green = 134, blue = 252),
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = HomeTab,
                    enterTransition = {
                        val from = tabIndexOf(initialState.destination)
                        val to = tabIndexOf(targetState.destination)
                        slideIntoContainer(if (to > from) SlideDirection.Start else SlideDirection.End)
                    },
                    exitTransition = {
                        val from = tabIndexOf(initialState.destination)
                        val to = tabIndexOf(targetState.destination)
                        slideOutOfContainer(if (to > from) SlideDirection.Start else SlideDirection.End)
                    },
                    popEnterTransition = { slideIntoContainer(SlideDirection.End) },
                    popExitTransition = { slideOutOfContainer(SlideDirection.End) },
                ) {
                    navigation<HomeTab>(startDestination = Home) {
                        composable<Home> { HomeScreen(contentVisible = isVisible) }
                    }
                    navigation<SearchTab>(startDestination = Search) {
                        composable<Search> { SearchScreen() }
                    }
                    navigation<FavoritesTab>(startDestination = Favorites) {
                        composable<Favorites> {
                            FavoritesScreen(onFavoriteClick = { name ->
                                navController.navigate(FavoriteDetail(name))
                            })
                        }
                        composable<FavoriteDetail> { entry ->
                            val route = entry.toRoute<FavoriteDetail>()
                            FavoriteDetailScreen(name = route.name)
                        }
                    }
                    navigation<SettingsTab>(startDestination = Settings) {
                        composable<Settings> { SettingsScreen() }
                    }
                    navigation<ProfileTab>(startDestination = Profile) {
                        composable<Profile> { ProfileScreen() }
                    }
                }
            }
        },
    )
}
