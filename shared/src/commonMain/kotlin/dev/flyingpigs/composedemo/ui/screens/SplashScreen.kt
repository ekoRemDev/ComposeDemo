package dev.flyingpigs.composedemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.flyingpigs.composedemo.ui.components.Logo
import kotlinx.coroutines.delay

/**
 * Brief branded screen shown on launch, then auto-advances.
 *
 * [LaunchedEffect] is the Compose way to run a side effect tied to the
 * composition lifecycle — it launches a coroutine when the screen enters
 * and cancels it if the screen leaves. The `Unit` key means "run once".
 * (Flutter analog: kicking off a Future in initState, but auto-cancelled.)
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(red = 187, green = 134, blue = 252)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Logo(modifier = Modifier.size(96.dp))
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = Color.White)
    }
}
