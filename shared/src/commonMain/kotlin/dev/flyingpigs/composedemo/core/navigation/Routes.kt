package dev.flyingpigs.composedemo.core.navigation

import kotlinx.serialization.Serializable


// root-level destinations (full screen, no bottom bar / top bar chrome)
@Serializable
object Splash
@Serializable
object Welcome

@Serializable
object Login
@Serializable
object Main

// tab graphs — what the bottom bar navigates to
@Serializable
object HomeTab
@Serializable
object SearchTab
@Serializable
object FavoritesTab
@Serializable
object ProfileTab
@Serializable
object SettingsTab

// screens
@Serializable
object Home
@Serializable
object Search
@Serializable
object Favorites
@Serializable
object Profile
@Serializable
object Settings


@Serializable
data class FavoriteDetail(val name: String)
