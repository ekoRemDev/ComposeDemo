package dev.flyingpigs.composedemo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GreetingLabel(
    greeting: String,                  // required: no default value -> caller MUST pass it
    prefix: String = "Compose:",       // optional: has a default -> caller MAY omit it
    modifier: Modifier = Modifier,     // optional: Compose convention
) {
    Text(
        "$prefix $greeting",
        modifier = modifier.padding(top = 16.dp, bottom = 2.dp, start = 8.dp, end = 8.dp),
    )
}