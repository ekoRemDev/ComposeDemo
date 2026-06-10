package dev.flyingpigs.composedemo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import composedemo.shared.generated.resources.Res
import composedemo.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

/** Stateless composable — the equivalent of a Flutter StatelessWidget. */
@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(
        painterResource(Res.drawable.compose_multiplatform),
        null,
        modifier = modifier.size(80.dp, 80.dp),
    )
}