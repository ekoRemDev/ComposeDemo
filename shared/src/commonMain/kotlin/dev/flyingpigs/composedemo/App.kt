package dev.flyingpigs.composedemo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.flyingpigs.composedemo.ui.navigation.Login
import dev.flyingpigs.composedemo.ui.navigation.Main
import dev.flyingpigs.composedemo.ui.navigation.Splash
import dev.flyingpigs.composedemo.ui.navigation.Welcome
import dev.flyingpigs.composedemo.ui.screens.LoginScreen
import dev.flyingpigs.composedemo.ui.screens.SplashScreen
import dev.flyingpigs.composedemo.ui.screens.WelcomeScreen

/**
 * Root router. Three full-screen, chrome-less destinations:
 *
 *   Splash  ──(after delay)──▶  Welcome  ──(Get Started)──▶  Main
 *
 * Each forward step uses popUpTo(..., inclusive = true) so the previous
 * destination is removed from the back stack — once you reach Main, pressing
 * back exits the app instead of returning to Welcome/Splash.
 *
 * Main is itself a screen that hosts the tabbed Scaffold (see MainScreen),
 * so the bottom bar / top bar only appear inside Main — nested navigation.
 */
@Composable
@Preview
fun App() {
    val rootNavController = rememberNavController()

    MaterialTheme {
        NavHost(
            navController = rootNavController,
            startDestination = Splash,
        ) {
            composable<Splash> {
                SplashScreen(
                    onTimeout = {
                        rootNavController.navigate(Welcome) {
                            popUpTo(Splash) { inclusive = true }
                        }
                    },
                )
            }
            composable<Welcome> {
                WelcomeScreen(
                    onContinue = {
                        rootNavController.navigate(Login) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                )
            }
            composable<Login> {
                LoginScreen(
                    onContinue = {
                        rootNavController.navigate(Main) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    },

                    )
            }
            composable<Main> {
                MainScreen(
                    onLogout = {
                        rootNavController.navigate(Login) {
                            popUpTo(Main) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
